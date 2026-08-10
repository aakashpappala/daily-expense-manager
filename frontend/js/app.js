// DAILY EXPENSE MANAGER - Frontend JavaScript Client
const API_BASE = 'https://daily-expense-manager-csk8.onrender.com/api';

let jwtToken = localStorage.getItem('jwtToken');
let currentUser = JSON.parse(localStorage.getItem('currentUser') || 'null');
let categoriesCache = [];
let categoryChartInstance = null;
let dailyChartInstance = null;

// Initialize App
document.addEventListener('DOMContentLoaded', () => {
    if (jwtToken && currentUser) {
        showApp();
    } else {
        showAuth();
    }
});

function showAuth() {
    document.getElementById('navbar').style.display = 'none';
    document.getElementById('auth-view').style.display = 'flex';
    hideAllViews();
}

function showApp() {
    document.getElementById('navbar').style.display = 'flex';
    document.getElementById('auth-view').style.display = 'none';
    document.getElementById('user-display-name').innerText = currentUser.fullName;
    loadUserCategories();
    switchView('dashboard');
}

function hideAllViews() {
    const views = document.querySelectorAll('.view-content');
    views.forEach(v => v.style.display = 'none');
}

function switchView(viewName) {
    hideAllViews();
    document.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));
    
    const targetView = document.getElementById(`${viewName}-view`);
    if (targetView) targetView.style.display = 'block';

    if (viewName === 'dashboard') {
        loadDashboard();
    } else if (viewName === 'expenses') {
        loadExpenses();
    } else if (viewName === 'categories') {
        loadCategoriesView();
    } else if (viewName === 'budgets') {
        loadBudgetsView();
    } else if (viewName === 'reports') {
        loadReportsView();
    } else if (viewName === 'notifications') {
        loadNotificationsView();
    }
    fetchUnreadNotificationsCount();
}

// API Helper
async function apiRequest(endpoint, method = 'GET', body = null, isFormData = false) {
    const headers = {};
    if (jwtToken) {
        headers['Authorization'] = `Bearer ${jwtToken}`;
    }
    if (body && !isFormData) {
        headers['Content-Type'] = 'application/json';
    }

    const config = { method, headers };
    if (body) {
        config.body = isFormData ? body : JSON.stringify(body);
    }

    try {
        const response = await fetch(`${API_BASE}${endpoint}`, config);
        if (response.status === 401 || response.status === 403) {
            logout();
            throw new Error("Session expired. Please login again.");
        }
        
        if (!response.ok) {
            const errData = await response.json();
            throw new Error(errData.message || 'Request failed');
        }

        if (response.status === 204) return null;
        const contentType = response.headers.get("content-type");
        if (contentType && contentType.includes("application/json")) {
            return await response.json();
        }
        return await response.blob();
    } catch (err) {
        showToast(err.message, 'error');
        throw err;
    }
}

// Auth Handlers
function toggleAuthTab(tab) {
    document.getElementById('tab-login').classList.toggle('active', tab === 'login');
    document.getElementById('tab-register').classList.toggle('active', tab === 'register');
    document.getElementById('login-form').style.display = tab === 'login' ? 'block' : 'none';
    document.getElementById('register-form').style.display = tab === 'register' ? 'block' : 'none';
}

async function handleLogin(e) {
    e.preventDefault();
    const email = document.getElementById('login-email').value;
    const password = document.getElementById('login-password').value;

    try {
        const data = await apiRequest('/auth/login', 'POST', { email, password });
        jwtToken = data.token;
        currentUser = data.user;
        localStorage.setItem('jwtToken', jwtToken);
        localStorage.setItem('currentUser', JSON.stringify(currentUser));
        showToast('Login successful!', 'success');
        showApp();
    } catch (err) {
        // Handled in apiRequest
    }
}

async function handleRegister(e) {
    e.preventDefault();
    const fullName = document.getElementById('reg-name').value;
    const email = document.getElementById('reg-email').value;
    const password = document.getElementById('reg-password').value;
    const confirmPassword = document.getElementById('reg-confirm-password').value;

    try {
        const data = await apiRequest('/auth/register', 'POST', { fullName, email, password, confirmPassword });
        jwtToken = data.token;
        currentUser = data.user;
        localStorage.setItem('jwtToken', jwtToken);
        localStorage.setItem('currentUser', JSON.stringify(currentUser));
        showToast('Account registered successfully!', 'success');
        showApp();
    } catch (err) {
        // Handled
    }
}

function logout() {
    jwtToken = null;
    currentUser = null;
    localStorage.removeItem('jwtToken');
    localStorage.removeItem('currentUser');
    showAuth();
}

// Load Categories Cache & Filter Dropdowns
async function loadUserCategories() {
    try {
        categoriesCache = await apiRequest('/categories');
        populateCategoryDropdowns();
    } catch (err) {}
}

function populateCategoryDropdowns() {
    const expenseCatSelect = document.getElementById('expense-category');
    const filterCatSelect = document.getElementById('filter-category');
    
    if (expenseCatSelect) {
        expenseCatSelect.innerHTML = categoriesCache.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
    }
    if (filterCatSelect) {
        filterCatSelect.innerHTML = '<option value="">All Categories</option>' + 
            categoriesCache.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
    }
}

// Dashboard Loader
async function loadDashboard() {
    try {
        const data = await apiRequest('/dashboard');

        // Metrics
        document.getElementById('metric-today-total').innerText = `₹${data.todayExpenseTotal.toFixed(2)}`;
        document.getElementById('metric-month-total').innerText = `₹${data.monthExpenseTotal.toFixed(2)}`;
        
        document.getElementById('metric-daily-budget-sub').innerText = `Limit: ₹${data.budget.dailyBudget.toFixed(2)}`;
        document.getElementById('metric-monthly-budget-sub').innerText = `Limit: ₹${data.budget.monthlyBudget.toFixed(2)}`;

        document.getElementById('metric-remaining-daily').innerText = `₹${data.budget.remainingDailyBudget.toFixed(2)}`;
        document.getElementById('metric-remaining-monthly').innerText = `₹${data.budget.remainingMonthlyBudget.toFixed(2)}`;

        document.getElementById('metric-daily-status').innerText = `Status: ${data.budget.dailyStatus}`;
        document.getElementById('metric-monthly-status').innerText = `Status: ${data.budget.monthlyStatus}`;

        // Status Card colors
        const dailyCard = document.getElementById('card-daily-rem');
        dailyCard.className = `metric-card ${data.budget.dailyStatus === 'EXCEEDED' ? 'danger' : data.budget.dailyStatus === 'WARNING' ? 'warning' : 'success'}`;

        const monthlyCard = document.getElementById('card-monthly-rem');
        monthlyCard.className = `metric-card ${data.budget.monthlyStatus === 'EXCEEDED' ? 'danger' : data.budget.monthlyStatus === 'WARNING' ? 'warning' : 'success'}`;

        // Alerts Banner
        const alertsContainer = document.getElementById('dashboard-alerts-container');
        if (data.activeAlerts && data.activeAlerts.length > 0) {
            alertsContainer.innerHTML = data.activeAlerts.map(alert => `
                <div class="alert-banner warning">
                    <span>${alert}</span>
                </div>
            `).join('');
        } else {
            alertsContainer.innerHTML = '';
        }

        // Category Spendings Grid
        const grid = document.getElementById('dashboard-category-grid');
        grid.innerHTML = data.categorySpendings.map(cat => `
            <div class="category-card ${cat.isHighSpending ? 'high-spending' : ''}" onclick="viewCategoryDetail(${cat.categoryId})">
                <div class="category-header">
                    <span class="category-name">${cat.categoryName}</span>
                    ${cat.isHighSpending ? '<span style="color:var(--danger); font-size:0.75rem; font-weight:700;">HIGH SPENDING</span>' : ''}
                </div>
                <div class="category-amount">₹${cat.currentMonthTotal.toFixed(2)}</div>
                <div style="font-size:0.8rem; color:var(--text-muted); margin-top:0.4rem;">
                    Last month: ₹${cat.previousMonthTotal.toFixed(2)} ${cat.percentageChange > 0 ? `<span style="color:var(--danger);">(+${cat.percentageChange}%)</span>` : ''}
                </div>
            </div>
        `).join('');

        // Recent Expenses Table
        renderExpensesTable(data.recentExpenses, 'recent-expenses-tbody');

    } catch (err) {}
}

// Category Detail View & Daily Detail Modal
async function viewCategoryDetail(categoryId) {
    try {
        const detail = await apiRequest(`/categories/${categoryId}/details`);
        hideAllViews();
        document.getElementById('category-detail-view').style.display = 'block';

        document.getElementById('cat-detail-title').innerText = `${detail.categoryName} — Detailed Breakdown`;
        document.getElementById('cat-detail-monthly-total').innerText = `₹${detail.monthlyTotal.toFixed(2)}`;
        document.getElementById('cat-detail-daily-avg').innerText = `₹${detail.averageDailySpending.toFixed(2)}`;
        
        document.getElementById('cat-detail-highest-day').innerText = detail.highestSpendingDay ? `${detail.highestSpendingDay.date} (₹${detail.highestSpendingDay.totalAmount.toFixed(2)})` : 'N/A';
        document.getElementById('cat-detail-lowest-day').innerText = detail.lowestSpendingDay ? `${detail.lowestSpendingDay.date} (₹${detail.lowestSpendingDay.totalAmount.toFixed(2)})` : 'N/A';

        const tbody = document.getElementById('cat-detail-daily-tbody');
        if (detail.dailyBreakdown.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" style="text-align:center;">No expenses recorded for this month.</td></tr>';
        } else {
            tbody.innerHTML = detail.dailyBreakdown.map(day => `
                <tr>
                    <td><strong>${day.date}</strong></td>
                    <td>${day.transactionCount} item(s)</td>
                    <td style="font-weight:700; color:var(--secondary);">₹${day.totalAmount.toFixed(2)}</td>
                    <td>
                        <button class="btn btn-secondary" onclick="viewDailyDetail(${categoryId}, '${day.date}')">View Daily Details</button>
                    </td>
                </tr>
            `).join('');
        }
    } catch (err) {}
}

async function viewDailyDetail(categoryId, dateStr) {
    try {
        const daily = await apiRequest(`/categories/${categoryId}/daily-details?date=${dateStr}`);
        const modal = document.getElementById('daily-detail-modal');
        document.getElementById('daily-modal-title').innerText = `${daily.categoryName} — ${daily.date} (Total: ₹${daily.dayTotal.toFixed(2)})`;
        
        const body = document.getElementById('daily-modal-body');
        body.innerHTML = `
            <div class="table-card">
                <table>
                    <thead>
                        <tr>
                            <th>Description</th>
                            <th>Payment</th>
                            <th>Amount</th>
                            <th>Bill</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${daily.expenses.map(e => `
                            <tr>
                                <td>${e.description || 'N/A'} ${e.location ? `<br><small style="color:var(--text-muted);">${e.location}</small>` : ''}</td>
                                <td><span class="user-badge">${e.paymentMethod}</span></td>
                                <td style="font-weight:700;">₹${e.amount.toFixed(2)}</td>
                                <td>
                                    ${e.hasBill ? `<button class="btn btn-secondary" onclick="viewBill(${e.id})">📄 View Bill</button>` : '<span style="color:var(--text-muted);">None</span>'}
                                </td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            </div>
        `;
        modal.classList.add('active');
    } catch (err) {}
}

function closeDailyDetailModal() {
    document.getElementById('daily-detail-modal').classList.remove('active');
}

// Expenses View
async function loadExpenses() {
    try {
        const expenses = await apiRequest('/expenses');
        renderExpensesTable(expenses, 'expenses-tbody');
    } catch (err) {}
}

async function applyExpenseFilters() {
    const q = document.getElementById('filter-query').value;
    const cat = document.getElementById('filter-category').value;
    const pm = document.getElementById('filter-pm').value;
    const start = document.getElementById('filter-start-date').value;
    const end = document.getElementById('filter-end-date').value;

    let url = '/expenses?';
    if (q) url += `query=${encodeURIComponent(q)}&`;
    if (cat) url += `categoryId=${cat}&`;
    if (pm) url += `paymentMethod=${encodeURIComponent(pm)}&`;
    if (start) url += `startDate=${start}&`;
    if (end) url += `endDate=${end}&`;

    try {
        const expenses = await apiRequest(url);
        renderExpensesTable(expenses, 'expenses-tbody');
    } catch (err) {}
}

function renderExpensesTable(expenses, tbodyId) {
    const tbody = document.getElementById(tbodyId);
    if (!expenses || expenses.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">No expenses found.</td></tr>';
        return;
    }

    tbody.innerHTML = expenses.map(e => `
        <tr>
            <td>${e.expenseDate}</td>
            <td><span class="user-badge" style="background:#475569;">${e.category.name}</span></td>
            <td>
                <strong>${e.description || 'Expense'}</strong>
                ${e.location ? `<br><small style="color:var(--text-muted);">📍 ${e.location}</small>` : ''}
            </td>
            <td>${e.paymentMethod}</td>
            <td style="font-weight:700; color:var(--secondary);">₹${e.amount.toFixed(2)}</td>
            <td>
                ${e.hasBill ? `
                    <div style="display:flex; gap:0.4rem;">
                        <button class="btn btn-secondary" onclick="viewBill(${e.id})">👁️ View</button>
                        <button class="btn btn-secondary" onclick="downloadBill(${e.id})">⬇️ Download</button>
                    </div>
                ` : '<span style="color:var(--text-muted);">No Bill</span>'}
            </td>
            <td>
                <button class="btn btn-danger" style="padding:0.4rem 0.8rem;" onclick="deleteExpense(${e.id})">Delete</button>
            </td>
        </tr>
    `).join('');
}

// Add Expense Modal Handler
function openExpenseModal() {
    document.getElementById('expense-form').reset();
    document.getElementById('expense-id').value = '';
    document.getElementById('expense-date').valueAsDate = new Date();
    document.getElementById('expense-modal-title').innerText = 'Add New Expense';
    document.getElementById('expense-modal').classList.add('active');
}

function closeExpenseModal() {
    document.getElementById('expense-modal').classList.remove('active');
}

async function saveExpense(e) {
    e.preventDefault();
    const categoryId = document.getElementById('expense-category').value;
    const amount = document.getElementById('expense-amount').value;
    const expenseDate = document.getElementById('expense-date').value;
    const paymentMethod = document.getElementById('expense-payment-method').value;
    const description = document.getElementById('expense-description').value;
    const location = document.getElementById('expense-location').value;
    const fileInput = document.getElementById('expense-file');

    const expensePayload = { categoryId, amount, expenseDate, paymentMethod, description, location };

    const formData = new FormData();
    formData.append('expense', new Blob([JSON.stringify(expensePayload)], { type: 'application/json' }));
    if (fileInput.files.length > 0) {
        formData.append('bill', fileInput.files[0]);
    }

    try {
        await apiRequest('/expenses', 'POST', formData, true);
        showToast('Expense saved successfully!', 'success');
        closeExpenseModal();
        loadDashboard();
    } catch (err) {}
}

// Rapido Quick Ride Modal (Java Method Overloading Integration)
function openRapidoModal() {
    document.getElementById('rapido-modal').classList.add('active');
}
function closeRapidoModal() {
    document.getElementById('rapido-modal').classList.remove('active');
}

async function saveRapidoRide(e) {
    e.preventDefault();
    const amount = document.getElementById('rapido-amount').value;
    const message = document.getElementById('rapido-message').value;
    const location = document.getElementById('rapido-location').value;

    try {
        await apiRequest('/expenses/rapido', 'POST', { amount, message, location });
        showToast('Rapido ride expense added!', 'success');
        closeRapidoModal();
        loadDashboard();
    } catch (err) {}
}

async function deleteExpense(id) {
    if (!confirm('Are you sure you want to delete this expense?')) return;
    try {
        await apiRequest(`/expenses/${id}`, 'DELETE');
        showToast('Expense deleted successfully', 'success');
        loadDashboard();
    } catch (err) {}
}

// Bill View / Download Handlers
async function viewBill(expenseId) {
    try {
        const blob = await apiRequest(`/expenses/${expenseId}/bill/view`);
        const url = URL.createObjectURL(blob);
        window.open(url, '_blank');
    } catch (err) {}
}

async function downloadBill(expenseId) {
    try {
        const blob = await apiRequest(`/expenses/${expenseId}/bill/download`);
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `bill_${expenseId}`;
        a.click();
    } catch (err) {}
}

// Custom Category Modal
function openCategoryModal() {
    document.getElementById('category-modal').classList.add('active');
}
function closeCategoryModal() {
    document.getElementById('category-modal').classList.remove('active');
}

async function saveCategory(e) {
    e.preventDefault();
    const name = document.getElementById('category-name-input').value;
    try {
        await apiRequest('/categories', 'POST', { name });
        showToast('Custom category created!', 'success');
        closeCategoryModal();
        loadUserCategories();
        loadCategoriesView();
    } catch (err) {}
}

async function loadCategoriesView() {
    try {
        const categories = await apiRequest('/categories');
        const grid = document.getElementById('all-categories-grid');
        grid.innerHTML = categories.map(c => `
            <div class="category-card" onclick="viewCategoryDetail(${c.id})">
                <div class="category-header">
                    <span class="category-name">${c.name}</span>
                    ${c.isDefault ? '<span class="user-badge">Default</span>' : '<span class="user-badge" style="background:#0284c7;">Custom</span>'}
                </div>
                <div style="font-size:0.85rem; color:var(--text-muted); margin-top:0.5rem;">Click to view detailed breakdown</div>
            </div>
        `).join('');
    } catch (err) {}
}

// Budgets View
async function loadBudgetsView() {
    try {
        const budget = await apiRequest('/budgets');
        document.getElementById('input-daily-budget').value = budget.dailyBudget;
        document.getElementById('input-monthly-budget').value = budget.monthlyBudget;
    } catch (err) {}
}

async function saveBudgets(e) {
    e.preventDefault();
    const dailyBudget = document.getElementById('input-daily-budget').value;
    const monthlyBudget = document.getElementById('input-monthly-budget').value;

    try {
        await apiRequest('/budgets', 'PUT', { dailyBudget, monthlyBudget });
        showToast('Budget limits updated!', 'success');
        loadDashboard();
    } catch (err) {}
}

// Reports & Charts View
async function loadReportsView() {
    try {
        const data = await apiRequest('/reports/analytics');

        // Analytics Table
        const tbody = document.getElementById('analytics-spending-tbody');
        tbody.innerHTML = data.categoryAnalyses.map(cat => `
            <tr>
                <td><strong>${cat.categoryName}</strong></td>
                <td style="font-weight:700;">₹${cat.currentMonthTotal.toFixed(2)}</td>
                <td>₹${cat.previousMonthTotal.toFixed(2)}</td>
                <td style="color:${cat.difference > 0 ? 'var(--danger)' : 'var(--success)'}">₹${cat.difference.toFixed(2)}</td>
                <td>${cat.percentageChange}%</td>
                <td>
                    ${cat.isWarningAlert ? `<span style="color:var(--danger); font-weight:700;">${cat.warningMessage}</span>` : '<span style="color:var(--success);">Normal</span>'}
                </td>
            </tr>
        `).join('');

        // Chart 1: Category Comparison
        const catCanvas = document.getElementById('chart-category-comparison');
        if (categoryChartInstance) categoryChartInstance.destroy();
        categoryChartInstance = new Chart(catCanvas, {
            type: 'bar',
            data: {
                labels: data.categoryAnalyses.map(c => c.categoryName),
                datasets: [
                    { label: 'This Month (₹)', data: data.categoryAnalyses.map(c => c.currentMonthTotal), backgroundColor: '#6366f1' },
                    { label: 'Last Month (₹)', data: data.categoryAnalyses.map(c => c.previousMonthTotal), backgroundColor: '#0ea5e9' }
                ]
            },
            options: { responsive: true, plugins: { legend: { labels: { color: '#fff' } } }, scales: { x: { ticks: { color: '#94a3b8' } }, y: { ticks: { color: '#94a3b8' } } } }
        });

        // Chart 2: Daily Spending Trend
        const dailyCanvas = document.getElementById('chart-daily-trend');
        if (dailyChartInstance) dailyChartInstance.destroy();
        dailyChartInstance = new Chart(dailyCanvas, {
            type: 'line',
            data: {
                labels: data.dailyTrends.map(d => d.label),
                datasets: [{ label: 'Daily Expense (₹)', data: data.dailyTrends.map(d => d.amount), borderColor: '#10b981', tension: 0.3, fill: true, backgroundColor: 'rgba(16, 185, 129, 0.1)' }]
            },
            options: { responsive: true, plugins: { legend: { labels: { color: '#fff' } } }, scales: { x: { ticks: { color: '#94a3b8' } }, y: { ticks: { color: '#94a3b8' } } } }
        });

    } catch (err) {}
}

// Notifications View
async function fetchUnreadNotificationsCount() {
    try {
        const res = await apiRequest('/notifications/unread-count');
        const badge = document.getElementById('nav-unread-badge');
        if (res.unreadCount > 0) {
            badge.innerText = res.unreadCount;
            badge.style.display = 'inline-block';
        } else {
            badge.style.display = 'none';
        }
    } catch (err) {}
}

async function loadNotificationsView() {
    try {
        const notifications = await apiRequest('/notifications');
        const container = document.getElementById('notifications-list-container');

        if (!notifications || notifications.length === 0) {
            container.innerHTML = '<div style="text-align:center; padding:2rem; color:var(--text-muted);">No notifications found.</div>';
            return;
        }

        container.innerHTML = notifications.map(n => `
            <div class="metric-card ${n.type === 'BUDGET_WARNING' || n.type === 'SPENDING_ALERT' ? 'warning' : ''}" style="margin-bottom:1rem; opacity:${n.isRead ? '0.7' : '1'};">
                <div style="display:flex; justify-content:space-between; align-items:center;">
                    <div style="font-size:1.05rem; font-weight:600;">${n.message}</div>
                    ${!n.isRead ? `<button class="btn btn-secondary" onclick="markNotificationRead(${n.id})">Mark as Read</button>` : '<span class="user-badge">Read</span>'}
                </div>
                <div style="font-size:0.8rem; color:var(--text-muted); margin-top:0.4rem;">${new Date(n.createdAt).toLocaleString()}</div>
            </div>
        `).join('');

    } catch (err) {}
}

async function markNotificationRead(id) {
    try {
        await apiRequest(`/notifications/${id}/read`, 'PUT');
        loadNotificationsView();
    } catch (err) {}
}

async function markAllNotificationsAsRead() {
    try {
        await apiRequest('/notifications/read-all', 'PUT');
        showToast('All notifications marked as read', 'success');
        loadNotificationsView();
    } catch (err) {}
}

// Toast Helper
function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerText = message;
    container.appendChild(toast);
    setTimeout(() => toast.remove(), 4000);
}
