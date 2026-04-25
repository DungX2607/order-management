// Admin User Management Logic
requireAdmin();

let users = [];

// Initialize page
async function init() {
    displayUserInfo();
    await loadUsers();
}

function displayUserInfo() {
    const userInfo = getUserInfo();
    if (userInfo) {
        document.getElementById('userInfo').textContent = `Admin: ${userInfo.fullName}`;
    }
}

async function loadUsers() {
    showLoading();
    
    try {
        users = await api.getUsers();
        renderUsers();
    } catch (error) {
        console.error('Failed to load users:', error);
        showError('Không thể tải danh sách thành viên');
        document.getElementById('usersContainer').innerHTML = 
            '<p style="color: var(--danger-color);">Không thể tải danh sách thành viên</p>';
    } finally {
        hideLoading();
    }
}

function renderUsers() {
    const container = document.getElementById('usersContainer');
    
    if (users.length === 0) {
        container.innerHTML = '<p>Chưa có thành viên nào.</p>';
        return;
    }
    
    // Separate admins and members
    const admins = users.filter(u => u.role === 'ADMIN');
    const members = users.filter(u => u.role === 'MEMBER');
    
    let html = '';
    
    // Admins section
    if (admins.length > 0) {
        html += '<h3>Quản trị viên</h3>';
        html += '<table><thead><tr>';
        html += '<th>Tên đăng nhập</th>';
        html += '<th>Họ và tên</th>';
        html += '<th>Trạng thái</th>';
        html += '<th>Ngày tạo</th>';
        html += '</tr></thead><tbody>';
        
        admins.forEach(user => {
            html += '<tr>';
            html += `<td>${user.username}</td>`;
            html += `<td>${user.fullName}</td>`;
            html += `<td>${user.active ? '<span class="status-badge status-open">Hoạt động</span>' : '<span class="status-badge status-closed">Khóa</span>'}</td>`;
            html += `<td>${formatDateTime(user.createdAt)}</td>`;
            html += '</tr>';
        });
        
        html += '</tbody></table>';
    }
    
    // Members section
    html += '<h3 style="margin-top: 2rem;">Thành viên</h3>';
    html += '<table><thead><tr>';
    html += '<th>Tên đăng nhập</th>';
    html += '<th>Họ và tên</th>';
    html += '<th>Trạng thái</th>';
    html += '<th>Ngày tạo</th>';
    html += '</tr></thead><tbody>';
    
    if (members.length === 0) {
        html += '<tr><td colspan="4" style="text-align: center; color: #666;">Chưa có thành viên nào</td></tr>';
    } else {
        members.forEach(user => {
            html += '<tr>';
            html += `<td>${user.username}</td>`;
            html += `<td>${user.fullName}</td>`;
            html += `<td>${user.active ? '<span class="status-badge status-open">Hoạt động</span>' : '<span class="status-badge status-closed">Khóa</span>'}</td>`;
            html += `<td>${formatDateTime(user.createdAt)}</td>`;
            html += '</tr>';
        });
    }
    
    html += '</tbody></table>';
    
    container.innerHTML = html;
}

async function submitUser(e) {
    e.preventDefault();
    
    const username = document.getElementById('username').value.trim();
    const fullName = document.getElementById('fullName').value.trim();
    
    const submitBtn = e.target.querySelector('button[type="submit"]');
    submitBtn.disabled = true;
    submitBtn.textContent = 'Đang thêm...';
    
    try {
        await api.createUser(username, fullName);
        showSuccess(`Đã thêm thành viên "${fullName}" thành công. Mật khẩu mặc định: 123456aA@`);
        
        // Reset form
        document.getElementById('userForm').reset();
        
        // Reload users
        await loadUsers();
    } catch (error) {
        showError(error.message || 'Không thể thêm thành viên');
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Thêm thành viên';
    }
}

// Event Listeners
document.addEventListener('DOMContentLoaded', init);
document.getElementById('userForm').addEventListener('submit', submitUser);
