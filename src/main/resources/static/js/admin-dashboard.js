// Admin Dashboard Logic
requireAdmin();

let currentCycle = null;
let orders = [];
let searchTimeout = null;

// Initialize page
async function init() {
    displayUserInfo();
    await loadCycleStatus();
    await loadOrders();
}

function displayUserInfo() {
    const userInfo = getUserInfo();
    if (userInfo) {
        document.getElementById('userInfo').textContent = `Admin: ${userInfo.fullName}`;
    }
}

async function loadCycleStatus() {
    try {
        currentCycle = await api.getCurrentCycle();
        updateCycleUI();
    } catch (error) {
        console.error('Failed to load cycle status:', error);
        showError('Không thể tải trạng thái chu kỳ');
    }
}

function updateCycleUI() {
    const statusDiv = document.getElementById('cycleStatus');
    const openBtn = document.getElementById('openCycleBtn');
    const closeBtn = document.getElementById('closeCycleBtn');
    
    if (currentCycle.status === 'OPEN') {
        const timeLeft = formatTimeRemaining(currentCycle.timeLeft);
        statusDiv.innerHTML = `
            <div style="display: flex; align-items: center; gap: 1rem;">
                <span class="status-badge status-open">Đang mở</span>
                <span>Còn lại: <strong>${timeLeft}</strong></span>
            </div>
        `;
        openBtn.disabled = true;
        closeBtn.disabled = false;
    } else {
        const nextCycle = formatDateTime(currentCycle.nextCycleTime);
        statusDiv.innerHTML = `
            <div style="display: flex; align-items: center; gap: 1rem;">
                <span class="status-badge status-closed">Đã đóng</span>
                <span>Chu kỳ tiếp theo: <strong>${nextCycle}</strong></span>
            </div>
        `;
        openBtn.disabled = false;
        closeBtn.disabled = true;
    }
}

async function openCycle() {
    if (!confirm('Bạn có chắc muốn mở chu kỳ mới?')) return;
    
    try {
        await api.openCycle();
        showSuccess('Đã mở chu kỳ mới');
        await loadCycleStatus();
        await loadOrders();
    } catch (error) {
        showError(error.message || 'Không thể mở chu kỳ');
    }
}

async function closeCycle() {
    if (!confirm('Bạn có chắc muốn đóng chu kỳ hiện tại?')) return;
    
    try {
        await api.closeCycle();
        showSuccess('Đã đóng chu kỳ');
        await loadCycleStatus();
        await loadOrders();
    } catch (error) {
        showError(error.message || 'Không thể đóng chu kỳ');
    }
}

async function loadOrders() {
    showLoading();
    
    const status = document.getElementById('statusFilter').value;
    const memberName = document.getElementById('searchInput').value.trim();
    
    try {
        orders = await api.getAllOrders(status || null, memberName || null);
        renderOrders();
        updateStatistics();
    } catch (error) {
        console.error('Failed to load orders:', error);
        showError('Không thể tải danh sách đơn hàng');
        document.getElementById('ordersContainer').innerHTML = 
            '<p style="color: var(--danger-color);">Không thể tải danh sách đơn hàng</p>';
    } finally {
        hideLoading();
    }
}

function renderOrders() {
    const container = document.getElementById('ordersContainer');
    
    if (orders.length === 0) {
        container.innerHTML = '<p>Chưa có đơn hàng nào trong chu kỳ này.</p>';
        return;
    }
    
    // Group by category
    const ordersByCategory = {};
    orders.forEach(order => {
        if (!ordersByCategory[order.categoryName]) {
            ordersByCategory[order.categoryName] = [];
        }
        ordersByCategory[order.categoryName].push(order);
    });
    
    let html = '<table><thead><tr>';
    html += '<th>STT</th>';
    html += '<th>Thành viên</th>';
    html += '<th>Danh mục</th>';
    html += '<th>Đồ uống</th>';
    html += '<th>Ghi chú</th>';
    html += '<th>Trạng thái</th>';
    html += '<th>Thao tác</th>';
    html += '</tr></thead><tbody>';
    
    let index = 1;
    Object.keys(ordersByCategory).sort().forEach(categoryName => {
        ordersByCategory[categoryName].forEach(order => {
            html += '<tr>';
            html += `<td>${index++}</td>`;
            html += `<td>${order.memberName}</td>`;
            html += `<td>${order.categoryName}</td>`;
            html += `<td>${order.menuItemName}</td>`;
            html += `<td>${order.note || '-'}</td>`;
            html += `<td>`;
            if (order.pickedUp) {
                html += '<span class="status-badge status-picked">Đã lấy</span>';
            } else {
                html += '<span class="status-badge status-unpicked">Chưa lấy</span>';
            }
            html += `</td>`;
            html += `<td>`;
            html += `<input type="checkbox" ${order.pickedUp ? 'checked' : ''} 
                     onchange="togglePickup(${order.id}, this.checked)">`;
            html += `</td>`;
            html += '</tr>';
        });
    });
    
    html += '</tbody></table>';
    container.innerHTML = html;
}

function updateStatistics() {
    const total = orders.length;
    const picked = orders.filter(o => o.pickedUp).length;
    const unpicked = total - picked;
    
    document.getElementById('totalOrders').textContent = total;
    document.getElementById('pickedOrders').textContent = picked;
    document.getElementById('unpickedOrders').textContent = unpicked;
}

async function togglePickup(orderId, isChecked) {
    try {
        await api.togglePickup(orderId);
        
        // Update local state
        const order = orders.find(o => o.id === orderId);
        if (order) {
            order.pickedUp = isChecked;
            updateStatistics();
        }
        
        showSuccess(isChecked ? 'Đã đánh dấu đã lấy' : 'Đã đánh dấu chưa lấy');
    } catch (error) {
        showError(error.message || 'Không thể cập nhật trạng thái');
        // Revert checkbox
        event.target.checked = !isChecked;
    }
}

function handleSearch() {
    // Debounce search
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
        loadOrders();
    }, 500);
}

function exportExcel() {
    try {
        api.exportExcel();
        showSuccess('Đang tải file Excel...');
    } catch (error) {
        showError('Không thể xuất file Excel');
    }
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', init);
