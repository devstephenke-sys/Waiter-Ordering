# Backend Deployment Guide for Waiter Order App

## Current State
The application is currently **local-only** with:
- Room SQLite database for offline storage
- No backend server
- No cloud synchronization
- No remote API endpoints

## Backend Architecture Options

### Option 1: Firebase Backend (Recommended for Quick Deployment)

Firebase provides a complete backend solution with minimal server-side code.

#### Prerequisites
- Firebase Console account
- Firebase project created
- `google-services.json` configured

#### Step 1: Enable Firebase Services

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select or create project: `devstephenke-sys/Waiter-Ordering`
3. Enable these services:
   - **Authentication** (Google Sign-In already configured)
   - **Firestore Database** (for real-time data sync)
   - **Cloud Functions** (for business logic)
   - **Cloud Storage** (for images/receipts)

#### Step 2: Configure Firestore

In your Firebase Console:
1. Go to **Build → Firestore Database**
2. Click **Create Database**
3. Select location (e.g., `europe-west3` for Kenya)
4. Choose **Start in Test Mode** (for development)
5. Set up these collections:

**Collections Structure:**
```
orders/
  {orderId}
    - orderId: string
    - tableName: string
    - waiterName: string
    - status: string (WAITING_FOR_PAYMENT, COMPLETED, CANCELLED)
    - totalAmount: number
    - paymentMethod: string (MPESA, CASH)
    - amountPaid: number
    - changeAmount: number
    - mpesaCode: string?
    - createdAt: timestamp
    - paidAt: timestamp?
    - notes: string?
    - items: array
      - itemName: string
      - unitPrice: number
      - quantity: number
      - subtotal: number

menuItems/
  {itemId}
    - name: string
    - category: string
    - price: number
    - emoji: string
    - createdAt: timestamp

dailySettlements/
  {settlementId}
    - dateString: string
    - closedAt: timestamp
    - waiterName: string
    - totalSales: number
    - mpesaSales: number
    - cashSales: number
    - unpaidAmount: number
    - totalOrdersCount: number
    - completedOrdersCount: number
    - notes: string?
```

#### Step 3: Firestore Security Rules

Create `firestore.rules` in project root:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Orders: Only authenticated users can read/write
    match /orders/{orderId} {
      allow read, write: if request.auth != null;
    }

    // Menu Items: Public read, authenticated write
    match /menuItems/{itemId} {
      allow read: if true;
      allow write: if request.auth != null;
    }

    // Daily Settlements: Only authenticated users
    match /dailySettlements/{settlementId} {
      allow read, write: if request.auth != null;
    }
  }
}
```

Deploy rules:
```bash
firebase deploy --only firestore:rules
```

#### Step 4: Add Firestore Dependencies

Uncomment in `app/build.gradle.kts`:
```kotlin
implementation(libs.firebase.firestore)
```

#### Step 5: Create Firestore Repository

Create `app/src/main/java/com/example/data/remote/FirestoreRepository.kt`:

```kotlin
package com.example.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
  private val db = FirebaseFirestore.getInstance()

  suspend fun syncOrder(orderData: Map<String, Any>): Result<Void> {
    return try {
      db.collection("orders")
        .add(orderData)
        .await()
      Result.success(null)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun syncMenuItem(itemData: Map<String, Any>): Result<Void> {
    return try {
      db.collection("menuItems")
        .add(itemData)
        .await()
      Result.success(null)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun getTodayOrders(): Result<List<Map<String, Any>>> {
    return try {
      val today = com.google.firebase.Timestamp.now()
      val snapshot = db.collection("orders")
        .whereGreaterThanOrEqualTo("createdAt", today)
        .get()
        .await()
      Result.success(snapshot.documents.map { it.data })
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
```

#### Step 6: Integrate with Existing App

Modify `OrderRepository` to include Firestore sync:

```kotlin
class OrderRepository(
  private val menuItemDao: MenuItemDao,
  private val orderDao: OrderDao,
  private val dailySettlementDao: DailySettlementDao,
  private val firestoreRepository: FirestoreRepository
) {
  suspend fun createOrder(...) = withContext(Dispatchers.IO) {
    // ... existing local logic ...

    // Sync to Firestore
    viewModelScope.launch {
      val orderMap = mapOf(
        "orderId" to orderId,
        "tableName" to tableName,
        "waiterName" to waiterName,
        "status" to status,
        "totalAmount" to totalAmount,
        "createdAt" to com.google.firebase.Timestamp(now / 1000, 0),
        "items" to items.map { it.toMap() }
      )
      firestoreRepository.syncOrder(orderMap)
    }

    orderId
  }
}
```

#### Step 7: Deploy Firebase

Install Firebase CLI:
```bash
npm install -g firebase-tools
firebase login
```

Initialize Firebase in project:
```bash
firebase init
```

Select:
- Firestore
- Functions (optional)

Deploy:
```bash
firebase deploy
```

---

### Option 2: Custom Node.js/Express Backend

For more control over business logic and API endpoints.

#### Prerequisites
- Node.js 18+ installed
- MongoDB or PostgreSQL (for database)
- Hosting platform (Render, Railway, AWS, etc.)

#### Step 1: Create Backend Project

```bash
mkdir waiter-orders-backend
cd waiter-orders-backend
npm init -y
npm install express mongoose cors dotenv
npm install -D nodemon typescript @types/node @types/express
```

#### Step 2: Project Structure

```
waiter-orders-backend/
├── src/
│   ├── config/
│   │   └── database.ts
│   ├── models/
│   │   ├── Order.ts
│   │   ├── MenuItem.ts
│   │   └── DailySettlement.ts
│   ├── routes/
│   │   ├── orders.ts
│   │   ├── menu.ts
│   │   └── settlements.ts
│   ├── middleware/
│   │   └── auth.ts
│   └── index.ts
├── .env
├── package.json
└── tsconfig.json
```

#### Step 3: Database Models (MongoDB)

`src/models/Order.ts`:
```typescript
import mongoose, { Schema } from 'mongoose';

const orderSchema = new Schema({
  orderNumber: { type: String, required: true },
  tableName: { type: String, required: true },
  waiterName: { type: String, required: true },
  status: {
    type: String,
    enum: ['WAITING_FOR_PAYMENT', 'COMPLETED', 'CANCELLED'],
    default: 'WAITING_FOR_PAYMENT'
  },
  totalAmount: { type: Number, required: true },
  paymentMethod: { type: String, enum: ['MPESA', 'CASH'] },
  amountPaid: { type: Number, default: 0 },
  changeAmount: { type: Number, default: 0 },
  mpesaCode: { type: String },
  items: [{
    itemName: String,
    unitPrice: Number,
    quantity: Number,
    subtotal: Number
  }],
  createdAt: { type: Date, default: Date.now },
  paidAt: { type: Date },
  notes: String
});

export default mongoose.model('Order', orderSchema);
```

#### Step 4: API Routes

`src/routes/orders.ts`:
```typescript
import express from 'express';
import Order from '../models/Order';

const router = express.Router();

// Create order
router.post('/', async (req, res) => {
  try {
    const order = await Order.create(req.body);
    res.status(201).json(order);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Get all orders
router.get('/', async (req, res) => {
  try {
    const orders = await Order.find().sort({ createdAt: -1 });
    res.json(orders);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Update order status
router.patch('/:id/status', async (req, res) => {
  try {
    const order = await Order.findByIdAndUpdate(
      req.params.id,
      { status: req.body.status, paidAt: Date.now() },
      { new: true }
    );
    res.json(order);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

export default router;
```

#### Step 5: Main Server

`src/index.ts`:
```typescript
import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import mongoose from 'mongoose';
import orderRoutes from './routes/orders';
import menuRoutes from './routes/menu';

dotenv.config();

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

mongoose.connect(process.env.MONGODB_URI || 'mongodb://localhost:27017/waiter-orders')
  .then(() => console.log('Connected to MongoDB'))
  .catch(err => console.error('MongoDB connection error:', err));

app.use('/api/orders', orderRoutes);
app.use('/api/menu', menuRoutes);

app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});
```

#### Step 6: Environment Variables

`.env`:
```env
MONGODB_URI=mongodb+srv://user:password@cluster.mongodb.net/waiter-orders
PORT=3000
JWT_SECRET=your-secret-key
```

#### Step 7: Deploy to Render

1. Push code to GitHub
2. Go to [Render.com](https://render.com)
3. Create new **Web Service**
4. Connect GitHub repository
5. Configure:
   - Build Command: `npm install && npm run build`
   - Start Command: `npm start`
   - Environment Variables from `.env`
6. Deploy

Your API will be available at: `https://your-app.onrender.com`

#### Step 8: Update Android App

Add API client to Android app:

```kotlin
// app/src/main/java/com/example/data/remote/ApiClient.kt
package com.example.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

data class OrderRequest(
    val tableName: String,
    val waiterName: String,
    val items: List<CartItem>,
    val totalAmount: Double
)

data class OrderResponse(
    val _id: String,
    val orderNumber: String,
    val tableName: String,
    val status: String,
    val totalAmount: Double
)

interface OrderApi {
    @POST("/api/orders")
    suspend fun createOrder(@Body order: OrderRequest): OrderResponse

    @GET("/api/orders")
    suspend fun getOrders(): List<OrderResponse>

    @PATCH("/api/orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") id: String,
        @Body status: Map<String, String>
    ): OrderResponse
}

object ApiClient {
    private const val BASE_URL = "https://your-app.onrender.com"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val orderApi: OrderApi = retrofit.create(OrderApi::class.java)
}
```

---

### Option 3: Supabase Backend (PostgreSQL-based)

Supabase provides an open-source Firebase alternative with PostgreSQL.

#### Prerequisites
- Supabase account
- Supabase project created

#### Step 1: Create Supabase Project

1. Go to [Supabase.com](https://supabase.com)
2. Create new project
3. Wait for database to be ready

#### Step 2: Set up Tables

In Supabase SQL Editor:

```sql
-- Orders table
CREATE TABLE orders (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    order_number TEXT NOT NULL,
    table_name TEXT NOT NULL,
    waiter_name TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'WAITING_FOR_PAYMENT',
    total_amount NUMERIC NOT NULL,
    payment_method TEXT,
    amount_paid NUMERIC DEFAULT 0,
    change_amount NUMERIC DEFAULT 0,
    mpesa_code TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    paid_at TIMESTAMP WITH TIME ZONE,
    notes TEXT
);

-- Menu items table
CREATE TABLE menu_items (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name TEXT NOT NULL,
    category TEXT NOT NULL,
    price NUMERIC NOT NULL,
    emoji TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Order items junction table
CREATE TABLE order_items (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    order_id UUID REFERENCES orders(id) ON DELETE CASCADE,
    item_name TEXT NOT NULL,
    unit_price NUMERIC NOT NULL,
    quantity INTEGER NOT NULL,
    subtotal NUMERIC NOT NULL
);

-- Daily settlements
CREATE TABLE daily_settlements (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    date_string TEXT NOT NULL,
    closed_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    waiter_name TEXT NOT NULL,
    total_sales NUMERIC NOT NULL,
    mpesa_sales NUMERIC NOT NULL,
    cash_sales NUMERIC NOT NULL,
    unpaid_amount NUMERIC NOT NULL,
    total_orders_count INTEGER NOT NULL,
    completed_orders_count INTEGER NOT NULL,
    notes TEXT
);
```

#### Step 3: Row Level Security (RLS)

```sql
-- Enable RLS
ALTER TABLE orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE menu_items ENABLE ROW LEVEL SECURITY;

-- Policies
CREATE POLICY "Authenticated users can access orders"
ON orders FOR ALL
TO authenticated
USING (true)
WITH CHECK (true);

CREATE POLICY "Public can read menu items"
ON menu_items FOR SELECT
TO public
USING (true);
```

#### Step 4: Add Supabase Dependencies

In `app/build.gradle.kts`:
```kotlin
implementation("io.github.jan-tennert.supabase:postgrest-kt:2.4.0")
implementation("io.github.jan-tennert.supabase:auth-kt:2.4.0")
implementation("io.ktor:ktor-client-android:2.3.7")
```

#### Step 5: Create Supabase Client

```kotlin
// app/src/main/java/com/example/data/remote/SupabaseClient.kt
package com.example.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {
    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = "YOUR_SUPABASE_URL",
        supabaseKey = "YOUR_SUPABASE_ANON_KEY"
    ) {
        install(Auth)
        install(Postgrest)
    }
}
```

#### Step 6: Use in Repository

```kotlin
class OrderRepository {
    suspend fun syncOrder(order: OrderEntity) {
        SupabaseClient.client.postgrest["orders"].insert(order)
    }
}
```

---

## Testing the Backend

### Firebase Testing
```bash
# Test Firestore rules
firebase emulators:start

# Run tests
firebase emulators:exec "npm test"
```

### Node.js Backend Testing
```bash
# Install testing dependencies
npm install -D jest supertest

# Run tests
npm test
```

### Supabase Testing
Use Supabase Dashboard to test queries directly.

---

## Authentication Integration

All backend options should integrate with Firebase Auth:

```kotlin
// Get Firebase ID token
val user = FirebaseAuth.getInstance().currentUser
val idToken = user?.getIdToken(false)?.await()?.token

// Add to API requests
@Header("Authorization", "Bearer $idToken")
```

---

## Production Checklist

- [ ] Backend deployed and accessible
- [ ] Database properly indexed
- [ ] Security rules/policies configured
- [ ] SSL/TLS enabled
- [ ] Monitoring/logging set up
- [ ] Backup strategy in place
- [ ] Rate limiting configured
- [ ] API documentation (Swagger/OpenAPI)
- [ ] Error handling implemented
- [ ] Health check endpoint added

---

## Recommended Option

**For this project, I recommend Firebase Backend because:**
1. Fastest to deploy (no server setup)
2. Real-time sync out of the box
3. Built-in authentication
4. Free tier for development
5. Scales automatically
6. Good documentation
7. Android SDK well-integrated

**Switch to Node.js/Express if you need:**
- Custom business logic
- Third-party API integrations
- Complex database queries
- Full control over infrastructure

**Choose Supabase if you prefer:**
- PostgreSQL over NoSQL
- Open-source solution
- SQL database access
- Similar features to Firebase
