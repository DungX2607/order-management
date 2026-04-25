// Member Order Page Logic
requireAuth();

let currentCycle = null;
let myOrder = null;
let menuItems = [];
let categories = [];

// Initialize page
async function init() {
    displayUserInfo();
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

function displayUserInfo() {
    const userInfo = getUserInfo();
    if (userInfo) {
        document.getElementById('userInfo').textContent = `Xin chào, ${userInfo.fullName}`;
    }
}

async function loadCycleStatus() {
    try {
        currentCycle = await api.getCurrentCycle();
        updateCycleUI();
    } catch (error) {
        console.error('Failed to load cycle status:', error);
        document.getElementById('cycleStatus').innerHTML = 
            '<p style="color: var(--danger-color);">Không thể tải trạng thái chu kỳ</p>';
    }
}

function updateCycleUI() {
    const statusDiv = document.getElementById('cycleStatus');
    
    if (currentCycle.status === 'OPEN') {
        const timeLeft = formatTimeRemaining(currentCycle.timeLeft);
        statusDiv.innerHTML = `
            <div style="display: flex; align-items: center; gap: 1rem;">
                <span class="status-badge status-open">Đang mở</span>
                <span>Còn lại: <strong>${timeLeft}</strong></span>
            </div>
        `;
        enableOrderForm();
    } else {
        const nextCycle = formatDateTime(currentCycle.nextCycleTime);
        statusDiv.innerHTML = `
            <div style="display: flex; align-items: center; gap: 1rem;">
                <span class="status-badge status-closed">Đã đóng</span>
                <span>Chu kỳ tiếp theo: <strong>${nextCycle}</strong></span>
            </div>
        `;
        disableOrderForm();
    }
}

async function loadMenu() {
    try {
        categories = await api.getCategories();
        menuItems = await api.getMenuItems();
        renderMenu();
    } catch (error) {
        console.error('Failed to load menu:', error);
        showError('Không thể tải menu');
    }
}

function renderMenu() {
    const menuDiv = document.getElementById('menu');
    menuDiv.innerHTML = '';
    
    if (categories.length === 0) {
        menuDiv.innerHTML = '<p>Chưa có menu nào. Vui lòng liên hệ Admin.</p>';
        return;
    }
    
    categories.forEach(category => {
        const items = menuItems.filter(item => 
            item.categoryId === category.id && item.active
        );
        
        if (items.length === 0) return;
        
        const categoryDiv = document.createElement('div');
        categoryDiv.className = 'menu-category';
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
            // Pre-fill form with existing order
            const radio = document.querySelector(`input[value="${myOrder.menuItemId}"]`);
            if (radio) radio.checked = true;
            
            document.getElementById('note').value = myOrder.note || '';
            document.getElementById('submitBtn').textContent = 'Cập nhật đơn hàng';
        }
    } catch (error) {
        // No order yet - this is fine
        myOrder = null;
        document.getElementById('submitBtn').textContent = 'Đặt hàng';
    }
}

function enableOrderForm() {
    const form = document.getElementById('orderForm');
    const inputs = form.querySelectorAll('input, textarea, button');
    inputs.forEach(input => input.disabled = false);
}

function disableOrderForm() {
    const form = document.getElementById('orderForm');
    const inputs = form.querySelectorAll('input, textarea, button');
    inputs.forEach(input => input.disabled = true);
    
    const submitBtn = document.getElementById('submitBtn');
    submitBtn.textContent = 'Chu kỳ đã đóng';
}

async function submitOrder(e) {
    e.preventDefault();
    
    const menuItemId = document.querySelector('input[name="menuItem"]:checked')?.value;
    const note = document.getElementById('note').value.trim();
    
    if (!menuItemId) {
        showError('Vui lòng chọn đồ uống');
        return;
    }
    
    const submitBtn = document.getElementById('submitBtn');
    const originalText = submitBtn.textContent;
    submitBtn.disabled = true;
    submitBtn.textContent = 'Đang xử lý...';
    
    try {
        if (myOrder) {
            await api.updateOrder(myOrder.id, parseInt(menuItemId), note);
            showSuccess('Đã cập nhật đơn hàng thành công!');
        } else {
            await api.createOrder(parseInt(menuItemId), note);
            showSuccess('Đã đặt hàng thành công!');
        }
        
        await loadMyOrder();
    } catch (error) {
        showError(error.message || 'Có lỗi xảy ra. Vui lòng thử lại.');
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = originalText;
    }
}

// Event Listeners
document.addEventListener('DOMContentLoaded', init);
document.getElementById('orderForm').addEventListener('submit', submitOrder);
