// API Base Configuration
const API_BASE_URL = '/api';
const TOKEN_KEY = 'auth_token';

// Token Management
function getToken() {
    return localStorage.getItem(TOKEN_KEY);
}

function saveToken(token) {
    localStorage.setItem(TOKEN_KEY, token);
}

function removeToken() {
    localStorage.removeItem(TOKEN_KEY);
}

// Generic API Call Function
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

// API Methods
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
