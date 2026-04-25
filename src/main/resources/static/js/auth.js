// Authentication Utilities

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
