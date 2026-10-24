# Google AI Studio Prompt: Smart Food Distribution Manager (Android App)

Copy the prompt below into Google AI Studio to generate an Android application for a small food distribution business operated by two people.

---

## Prompt

You are a senior Android developer, mobile UI/UX designer, and practical software architect. Build a **working, beginner-friendly Android application** called **Smart Food Distribution Manager** for a small food distribution business run by two family members.

The app must help both business owners manage daily operations in one place. It should be practical, easy to learn, reliable, and suitable for a student project. Do not create only a visual mockup: implement the app's main screens and working interactions.

### 1. Main goal

Reduce mistakes and save time when managing:
- Customer orders
- Product inventory and low-stock levels
- Food product expiry dates
- Customer payments and outstanding balances
- Deliveries and dispatch status
- Basic sales summaries

### 2. Platform and implementation requirements

- Target **Android phones** first.
- Use **Kotlin and Jetpack Compose** for a native Android app, unless the current Google AI Studio environment explicitly supports a different Android implementation more reliably. If so, explain the choice and keep the result Android-compatible.
- Use a clear, modular project structure and readable code suitable for a beginner to understand.
- Use Material 3 components, responsive layouts, accessible touch targets, and a consistent design system.
- Use a clean, professional visual style: white or warm off-white backgrounds, deep green accents, restrained use of orange for warnings, clear typography, and simple icons.
- Support portrait phone layouts and scrolling on small screens.
- Include sample/demo data so the app is useful immediately.
- Do not leave important buttons as decorative controls. Core actions must work.
- Provide setup and run instructions, dependencies, and any required configuration.
- Never hard-code API keys or secrets into source code.

### 3. Main navigation

Create a bottom navigation bar with:
1. **Dashboard**
2. **Orders**
3. **Inventory**
4. **Payments**
5. **More** — contains Deliveries, Reports, and Settings

The dashboard should display useful summary cards:
- Orders awaiting processing
- Products low in stock
- Products nearing expiry
- Total outstanding customer payments
- Deliveries still pending

Each card should open the relevant filtered screen when tapped.

### 4. Product inventory

Create an Inventory screen with:
- Product name
- SKU or product code
- Category
- Quantity available
- Unit (packet, box, carton, kg, litre, etc.)
- Purchase price and selling price
- Low-stock threshold
- Expiry date when relevant
- Supplier name (optional)

Users must be able to:
- Add, edit, view, and delete products, with confirmation before deletion
- Search by product name or code
- Filter by category, low stock, and expiring soon
- Increase or decrease stock with a reason (purchase received, sale/order, damage, correction)
- See clear low-stock warnings
- See products nearing expiry, with configurable warning period (default 30 days)
- Sort products by name, quantity, or expiry date

Use **first-expiry-first-out (FEFO)** as a suggested dispatch principle for eligible products, while allowing the user to verify batch and product suitability. Never assume an expired food product is safe to sell.

### 5. Customer orders

Create an Orders screen with:
- Order number generated automatically
- Customer/shop name
- Contact number (optional)
- Order date
- Product line items, quantity, and unit price
- Subtotal and final amount
- Notes (optional)
- Status: Draft, Confirmed, Packed, Dispatched, Delivered, or Cancelled

Users must be able to:
- Create, edit, view, search, and filter orders
- Add multiple products to one order
- Calculate totals automatically
- Update order status
- Show a clear confirmation before cancelling an order
- Prevent accidental duplicate stock deductions
- Deduct stock at a clearly defined point (preferably when an order is marked Dispatched), and reverse or adjust it safely if the workflow is cancelled
- Warn when requested quantity exceeds available stock; do not silently allow negative stock
- View order details and a simple printable/shareable order summary if supported

### 6. Customer and payment tracking

Create customer records with:
- Customer/shop name
- Optional phone number and address
- Notes

For each customer, show:
- Total billed
- Total payments received
- Outstanding balance

Allow users to:
- Record a payment with date, amount, method (cash, bank transfer, UPI, other), and optional reference/notes
- View payment history
- Mark an invoice as paid or partially paid through recorded transactions
- See overdue balances and payment due dates
- Search customers and filter customers with outstanding balances

**Financial correctness:** Never treat an order as paid merely because the user opens it. Update balances from recorded invoices/orders and payment transactions. Validate that payments are positive and do not silently apply a payment to the wrong customer.

### 7. Delivery and dispatch tracking

Create a Deliveries screen that lists orders ready for dispatch or already dispatched. Include:
- Order number
- Customer/shop
- Delivery address if available
- Dispatch date
- Delivery status: Pending, Dispatched, Delivered, or Failed
- Driver or delivery note (optional)

Allow users to update delivery status and view pending deliveries. Avoid requiring GPS or maps for the first version.

### 8. Reports

Create a simple Reports screen showing:
- Sales totals for today, this week, and this month
- Number of orders by status
- Best-selling products based on saved order data
- Low-stock products
- Products nearing expiry
- Outstanding payments

Use clear cards and simple charts if supported. Make the selected date range visible. Do not generate fake statistics when there is no data; show an empty state or demo data clearly labelled as sample data.

### 9. AI features — optional second phase

Build the core business app first. Keep AI functionality modular so it can be added safely after the basic app works.

Possible AI features:
- **Restocking suggestions:** estimate which products may need replenishment from historical sales and current stock. Explain the basis of each suggestion and label predictions as estimates, not guarantees.
- **Natural-language business summaries:** answer questions such as “Which products are low in stock?” using the app's actual saved data.
- **Order entry assistance:** convert pasted order text into a draft order for the user to review before saving.

If using Gemini or another AI service:
- Use a secure backend or a supported secure configuration for API calls; do not embed a private API key in the Android client.
- Send only the minimum data needed and avoid transmitting customer personal or financial details unless essential and explicitly configured.
- Treat model output as untrusted. Validate product IDs, quantities, prices, and dates in application code.
- Require the user to review and confirm AI-generated orders or restocking suggestions before any data changes.
- Do not let AI autonomously change inventory, payment balances, or order status.
- If AI credentials are unavailable, the app must still build and its core features must still work. Show a clear optional-feature setup message instead of crashing.

### 10. Data storage and reliability

- Persist products, customers, orders, order items, payments, inventory movements, and deliveries so data remains after closing and reopening the app.
- Prefer Room (SQLite) for a native Android app. If the chosen environment uses a different supported stack, select a persistent local database and explain why.
- Use stable IDs and timestamps.
- Keep inventory changes and the associated order status update consistent; avoid duplicate stock deductions when a user taps twice.
- Add validation for required fields, invalid quantities, negative prices, invalid dates, and malformed phone numbers where applicable.
- Provide loading, success, error, empty, and confirmation states.
- Include a basic backup/export option such as CSV where practical, and clearly explain how data can be restored. Do not claim data is cloud-backed up unless cloud sync is actually implemented.
- For this initial version, prioritize reliable single-device use. If multiple phones need to share live data, explain that a backend/cloud database and authentication are needed; do not pretend local storage synchronizes automatically.

### 11. User experience

- Make common actions easy to reach: “Add Order”, “Add Product”, and “Record Payment”.
- Use clear language, not technical jargon.
- Use large touch targets and accessible text contrast.
- Show warning labels using both text and color; do not rely on color alone.
- Add helpful confirmation dialogs before destructive actions.
- Include sensible empty states with a button to add the first item.
- Make the app easy for two non-technical business users to learn.
- If feasible, prepare the structure for English and Malayalam language support, but keep the first version's text consistent and avoid half-translated screens.

### 12. Demo data

Include clearly labelled sample records for:
- A few packaged food products with quantities and expiry dates
- Two or three shop customers
- A few orders in different statuses
- One partial payment and one outstanding balance
- A pending delivery

Ensure sample data is visibly identified as demo data and can be cleared. Do not mix demo transactions into real records without warning.

### 13. Testing and acceptance criteria

Before declaring the app complete, verify these flows:
1. Add a product and confirm it remains after restarting the app.
2. Create an order with multiple products and verify the total.
3. Mark an order as dispatched and confirm stock changes exactly once.
4. Try ordering more than available stock and verify a clear warning.
5. Record a partial payment and verify the remaining balance.
6. Update a delivery status and confirm it appears correctly in the list.
7. Search and filter products and orders.
8. Verify low-stock and near-expiry warnings.
9. Confirm that cancelling or editing an order does not corrupt inventory or payment records.
10. Confirm the app remains usable when optional AI credentials are not configured.

### 14. What to deliver

Produce:
1. The complete runnable project, not just pseudocode or a plan.
2. All necessary source files and configuration.
3. A short architecture explanation.
4. Step-by-step instructions to run it in the supported Google AI Studio workflow and, if needed, Android Studio.
5. Any required database setup or environment configuration.
6. A list of implemented features versus features left for a later phase.
7. A brief manual testing checklist.
8. Clear limitations, especially around cloud synchronization, backup, and AI/API setup.

### 15. Work in manageable steps

First inspect the capabilities of the current environment and state the implementation approach. Then create the core app with persistent storage, navigation, inventory, orders, payments, and delivery tracking. Run available checks, fix errors, and only then add reports and optional AI features. If the full project is too large for one pass, implement a coherent runnable MVP first and list the next steps instead of returning disconnected code fragments.

**Start now by building the smallest complete, runnable version of Smart Food Distribution Manager. Prioritize correctness, persistence, and usable workflows over decorative complexity.**
