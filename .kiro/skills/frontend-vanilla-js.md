---
name: frontend-vanilla-js
description: Kỹ năng xây dựng frontend với HTML/CSS/JS thuần, không dùng framework
tags: [javascript, html, css, frontend, vanilla-js]
---

# Frontend Development với Vanilla JavaScript

## Project Structure

```
static/
├── index.html              # Landing/Login page
├── member/
│   └── order.html         # Member order page
├── admin/
│   ├── dashboard.html     # Admin dashboard
│   ├── menu.html          # Menu management
│   └── users.html         # User management
├── css/
│   └── style.css          # Shared styles
└── js/
    ├── auth.js            # Authentication utilities
    ├── api.js             # API call utilities
    ├── order.js           # Order page logic
    ├── admin-dashboard.js # Dashboard logic
    ├── admin-menu.js      # Menu management logic
    └── admin-users.js     # User management logic
```

## API Utilities (api.js)

### Base API Client
```javascript
const API_BASE_URL = '/api';
const TOKEN_KEY = 'auth_token';

// Get token from localStorage
function getToken() {
    return localStorage.getItem(TOKEN_KEY);
}

// Save token to localStorage
function saveToken(token) {
    localStorage.setItem(TOKEN_KEY, token);
}

// Remove token from localStorage
function removeToken() {
    localStorage.removeItem(TOKEN_KEY);
}

// Generic API call function
async function callApi(endpoint, method = 'GET', body = null) {
    const token = getToken();
    const headers = {
        'Content-Type': 'application/json'
    };
    
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    
    const options = {
        method,
        headers
    };
    
    if (body) {
        options.body = JSON.stringify(body);
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, options);
        
        // Handle 401 - redirect to login
        if (response.status === 401) {
            removeToken();
            window.location.href = '/index.html';
            return;
        }
        
        // Handle other errors
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Request failed');
        }
        
        // Handle 204 No Content
        if (response.status === 204) {
            return null;
        }
        
        return await response.json();
    } catch (error) {
        console.error('API call failed:', error);
        throw error;
    }
}

// Specific API methods
const api = {
    // Auth
    login: (username, password) => 
        callApi('/auth/login', 'POST', { username, password }),
    
    logout: () => 
        callApi('/auth/logout', 'POST'),
    
    // Users
    getUsers: () => 
        callApi('/users'),
    
    createUser: (username, fullName) => 
        callApi('/users', 'POST', { username, fullName }),
    
    // Categories
    getCategories: () => 
        callApi('/categories'),
    
    createCategory: (name, displayOrder) => 
        callApi('/categories', 'POST', { name, displayOrder }),
    
    updateCategory: (id, name, displayOrder) => 
        callApi(`/categories/${id}`, 'PUT', { name, displayOrder }),
    
    deleteCategory: (id) => 
        callApi(`/categories/${id}`, 'DELETE'),
    
    // Menu Items
    getMenuItems: () => 
        callApi('/menu-items'),
    
    createMenuItem: (categoryId, name, displayOrder) => 
        callApi('/menu-items', 'POST', { categoryId, name, displayOrder }),
    
    updateMenuItem: (id, name, displayOrder) => 
        callApi(`/menu-items/${id}`, 'PUT', { name, displayOrder }),
    
    deleteMenuItem: (id) => 
        callApi(`/menu-items/${id}`, 'DELETE'),
    
    // Cycles
    getCurrentCycle: () => 
        callApi('/cycles/current'),
    
    openCycle: () => 
        callApi('/cycles/open', 'POST'),
    
    closeCycle: () => 
        callApi('/cycles/close', 'POST'),
    
    // Orders
    getMyOrder: () => 
        callApi('/orders/my'),
    
    createOrder: (menuItemId, note) => 
        callApi('/orders', 'POST', { menuItemId, note }),
    
    updateOrder: (id, menuItemId, note) => 
        callApi(`/orders/${id}`, 'PUT', { menuItemId, note }),
    
    getAllOrders: (status = null, memberName = null) => {
        let url = '/orders';
        const params = new URLSearchParams();
        if (status) params.append('status', status);
        if (memberName) params.append('memberName', memberName);
        if (params.toString()) url += `?${params.toString()}`;
        return callApi(url);
    },
    
    togglePickup: (id) => 
        callApi(`/orders/${id}/pickup`, 'PATCH'),
    
    // Export
    exportExcel: () => {
        const token = getToken();
        window.open(`${API_BASE_URL}/export/excel?token=${token}`, '_blank');
    }
};
```

## Authentication (auth.js)

```javascript
// Check if user is logged in
function isLoggedIn() {
    return !!getToken();
}

// Get user info from token (decode JWT)
function getUserInfo() {
    const token = getToken();
    if (!token) return null;
    
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        return {
            username: payload.sub,
            role: payload.role,
            fullName: payload.fullName
        };
    } catch (error) {
        console.error('Failed to decode token:', error);
        return null;
    }
}

// Check if user has admin role
function isAdmin() {
    const userInfo = getUserInfo();
    return userInfo && userInfo.role === 'ADMIN';
}

// Protect page - redirect if not logged in
function requireAuth() {
    if (!isLoggedIn()) {
        window.location.href = '/index.html';
    }
}

// Protect admin page
function requireAdmin() {
    requireAuth();
    if (!isAdmin()) {
        alert('Bạn không có quyền truy cập trang này');
        window.location.href = '/member/order.html';
    }
}

// Logout
async function logout() {
    try {
        await api.logout();
    } catch (error) {
        console.error('Logout failed:', error);
    } finally {
        removeToken();
        window.location.href = '/index.html';
    }
}
```

## UI Utilities

### Show/Hide Loading
```javascript
function showLoading(elementId = 'loading') {
    const loading = document.getElementById(elementId);
    if (loading) loading.style.display = 'block';
}

function hideLoading(elementId = 'loading') {
    const loading = document.getElementById(elementId);
    if (loading) loading.style.display = 'none';
}
```

### Show Messages
```javascript
function showMessage(message, type = 'success') {
    const messageDiv = document.createElement('div');
    messageDiv.className = `message message-${type}`;
    messageDiv.textContent = message;
    
    document.body.appendChild(messageDiv);
    
    setTimeout(() => {
        messageDiv.remove();
    }, 3000);
}

function showError(message) {
    showMessage(message, 'error');
}

function showSuccess(message) {
    showMessage(message, 'success');
}
```

### Format Time
```javascript
function formatTimeRemaining(seconds) {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    return `${hours}h ${minutes}m`;
}

function formatDateTime(isoString) {
    const date = new Date(isoString);
    return date.toLocaleString('vi-VN');
}
```

## Example: Login Page (index.html + login.js)

### HTML
```html
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng nhập - Hệ thống Đặt Nước</title>
    <link rel="stylesheet" href="/css/style.css">
</head>
<body>
    <div class="login-container">
        <h1>Đăng nhập</h1>
        <form id="loginForm">
            <div class="form-group">
                <label for="username">Tên đăng nhập</label>
                <input type="text" id="username" required>
            </div>
            <div class="form-group">
                <label for="password">Mật khẩu</label>
                <input type="password" id="password" required>
            </div>
            <button type="submit">Đăng nhập</button>
            <div id="error" class="error-message"></div>
        </form>
    </div>
    
    <script src="/js/api.js"></script>
    <script src="/js/auth.js"></script>
    <script>
        // Redirect if already logged in
        if (isLoggedIn()) {
            const userInfo = getUserInfo();
            if (userInfo.role === 'ADMIN') {
                window.location.href = '/admin/dashboard.html';
            } else {
                window.location.href = '/member/order.html';
            }
        }
        
        document.getElementById('loginForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            
            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;
            const errorDiv = document.getElementById('error');
            
            try {
                const response = await api.login(username, password);
                saveToken(response.token);
                
                // Redirect based on role
                if (response.role === 'ADMIN') {
                    window.location.href = '/admin/dashboard.html';
                } else {
                    window.location.href = '/member/order.html';
                }
            } catch (error) {
                errorDiv.textContent = error.message || 'Đăng nhập thất bại';
            }
        });
    </script>
</body>
</html>
```

## Example: Member Order Page

```javascript
// member/order.js
requireAuth(); // Protect page

let currentCycle = null;
let myOrder = null;
let menuItems = [];

async function init() {
    showLoading();
    try {
        await Promise.all([
            loadCycleStatus(),
            loadMenu(),
            loadMyOrder()
        ]);
    } catch (error) {
        showError('Không thể tải dữ liệu: ' + error.message);
    } finally {
        hideLoading();
    }
}

async function loadCycleStatus() {
    currentCycle = await api.getCurrentCycle();
    updateCycleUI();
}

function updateCycleUI() {
    const statusDiv = document.getElementById('cycleStatus');
    
    if (currentCycle.status === 'OPEN') {
        const timeLeft = formatTimeRemaining(currentCycle.timeLeft);
        statusDiv.innerHTML = `
            <span class="status-open">Đang mở</span>
            <span>Còn lại: ${timeLeft}</span>
        `;
        enableOrderForm();
    } else {
        const nextCycle = formatDateTime(currentCycle.nextCycleTime);
        statusDiv.innerHTML = `
            <span class="status-closed">Đã đóng</span>
            <span>Chu kỳ tiếp theo: ${nextCycle}</span>
        `;
        disableOrderForm();
    }
}

async function loadMenu() {
    const categories = await api.getCategories();
    menuItems = await api.getMenuItems();
    
    const menuDiv = document.getElementById('menu');
    menuDiv.innerHTML = '';
    
    categories.forEach(category => {
        const items = menuItems.filter(item => item.categoryId === category.id);
        
        const categoryDiv = document.createElement('div');
        categoryDiv.className = 'category';
        categoryDiv.innerHTML = `
            <h3>${category.name}</h3>
            <div class="menu-items">
                ${items.map(item => `
                    <label>
                        <input type="radio" name="menuItem" value="${item.id}">
                        ${item.name}
                    </label>
                `).join('')}
            </div>
        `;
        
        menuDiv.appendChild(categoryDiv);
    });
}

async function loadMyOrder() {
    try {
        myOrder = await api.getMyOrder();
        if (myOrder) {
            document.querySelector(`input[value="${myOrder.menuItemId}"]`).checked = true;
            document.getElementById('note').value = myOrder.note || '';
        }
    } catch (error) {
        // No order yet
        myOrder = null;
    }
}

async function submitOrder(e) {
    e.preventDefault();
    
    const menuItemId = document.querySelector('input[name="menuItem"]:checked')?.value;
    const note = document.getElementById('note').value;
    
    if (!menuItemId) {
        showError('Vui lòng chọn đồ uống');
        return;
    }
    
    try {
        if (myOrder) {
            await api.updateOrder(myOrder.id, menuItemId, note);
            showSuccess('Đã cập nhật đơn hàng');
        } else {
            await api.createOrder(menuItemId, note);
            showSuccess('Đã đặt hàng thành công');
        }
        await loadMyOrder();
    } catch (error) {
        showError(error.message);
    }
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', init);
document.getElementById('orderForm').addEventListener('submit', submitOrder);
```

## CSS Best Practices

```css
/* style.css */

/* Variables */
:root {
    --primary-color: #4CAF50;
    --danger-color: #f44336;
    --text-color: #333;
    --border-color: #ddd;
    --bg-color: #f5f5f5;
}

/* Reset */
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

body {
    font-family: Arial, sans-serif;
    color: var(--text-color);
    background-color: var(--bg-color);
}

/* Form styles */
.form-group {
    margin-bottom: 1rem;
}

.form-group label {
    display: block;
    margin-bottom: 0.5rem;
    font-weight: bold;
}

.form-group input,
.form-group textarea,
.form-group select {
    width: 100%;
    padding: 0.5rem;
    border: 1px solid var(--border-color);
    border-radius: 4px;
}

/* Button styles */
button {
    padding: 0.75rem 1.5rem;
    background-color: var(--primary-color);
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
}

button:hover {
    opacity: 0.9;
}

button:disabled {
    background-color: #ccc;
    cursor: not-allowed;
}

/* Message styles */
.message {
    padding: 1rem;
    margin: 1rem 0;
    border-radius: 4px;
}

.message-success {
    background-color: #d4edda;
    color: #155724;
}

.message-error {
    background-color: #f8d7da;
    color: #721c24;
}

/* Loading spinner */
.loading {
    display: none;
    text-align: center;
    padding: 2rem;
}
```
