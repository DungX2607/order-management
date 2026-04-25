// Admin Menu Management Logic
requireAdmin();

let categories = [];
let menuItems = [];

// Initialize page
async function init() {
    displayUserInfo();
    await loadMenu();
}

function displayUserInfo() {
    const userInfo = getUserInfo();
    if (userInfo) {
        document.getElementById('userInfo').textContent = `Admin: ${userInfo.fullName}`;
    }
}

async function loadMenu() {
    showLoading();
    
    try {
        categories = await api.getCategories();
        menuItems = await api.getMenuItems();
        
        updateCategorySelect();
        renderMenu();
    } catch (error) {
        console.error('Failed to load menu:', error);
        showError('Không thể tải menu');
    } finally {
        hideLoading();
    }
}

function updateCategorySelect() {
    const select = document.getElementById('itemCategory');
    select.innerHTML = '<option value="">-- Chọn danh mục --</option>';
    
    categories.forEach(category => {
        const option = document.createElement('option');
        option.value = category.id;
        option.textContent = category.name;
        select.appendChild(option);
    });
}

function renderMenu() {
    const container = document.getElementById('menuContainer');
    
    if (categories.length === 0) {
        container.innerHTML = '<p>Chưa có danh mục nào. Hãy thêm danh mục mới.</p>';
        return;
    }
    
    let html = '';
    
    categories.forEach(category => {
        const items = menuItems.filter(item => item.categoryId === category.id);
        
        html += `<div class="menu-category" style="margin-bottom: 2rem;">`;
        html += `<div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">`;
        html += `<h3 style="margin: 0;">${category.name} (Thứ tự: ${category.displayOrder})</h3>`;
        html += `<button onclick="deleteCategory(${category.id})" class="btn-danger btn-small">Xóa danh mục</button>`;
        html += `</div>`;
        
        if (items.length === 0) {
            html += '<p style="color: #666; font-style: italic;">Chưa có món nào</p>';
        } else {
            html += '<table><thead><tr>';
            html += '<th>Tên món</th>';
            html += '<th>Thứ tự</th>';
            html += '<th>Trạng thái</th>';
            html += '<th>Thao tác</th>';
            html += '</tr></thead><tbody>';
            
            items.sort((a, b) => a.displayOrder - b.displayOrder).forEach(item => {
                html += '<tr>';
                html += `<td>${item.name}</td>`;
                html += `<td>${item.displayOrder}</td>`;
                html += `<td>${item.active ? '<span class="status-badge status-open">Hoạt động</span>' : '<span class="status-badge status-closed">Ẩn</span>'}</td>`;
                html += `<td><button onclick="deleteMenuItem(${item.id})" class="btn-danger btn-small">Xóa</button></td>`;
                html += '</tr>';
            });
            
            html += '</tbody></table>';
        }
        
        html += `</div>`;
    });
    
    container.innerHTML = html;
}

async function submitCategory(e) {
    e.preventDefault();
    
    const name = document.getElementById('categoryName').value.trim();
    const displayOrder = parseInt(document.getElementById('categoryOrder').value);
    
    try {
        await api.createCategory(name, displayOrder);
        showSuccess('Đã thêm danh mục thành công');
        
        // Reset form
        document.getElementById('categoryForm').reset();
        
        // Reload menu
        await loadMenu();
    } catch (error) {
        showError(error.message || 'Không thể thêm danh mục');
    }
}

async function submitMenuItem(e) {
    e.preventDefault();
    
    const categoryId = parseInt(document.getElementById('itemCategory').value);
    const name = document.getElementById('itemName').value.trim();
    const displayOrder = parseInt(document.getElementById('itemOrder').value);
    
    if (!categoryId) {
        showError('Vui lòng chọn danh mục');
        return;
    }
    
    try {
        await api.createMenuItem(categoryId, name, displayOrder);
        showSuccess('Đã thêm món thành công');
        
        // Reset form
        document.getElementById('menuItemForm').reset();
        
        // Reload menu
        await loadMenu();
    } catch (error) {
        showError(error.message || 'Không thể thêm món');
    }
}

async function deleteCategory(id) {
    const category = categories.find(c => c.id === id);
    if (!category) return;
    
    const items = menuItems.filter(item => item.categoryId === id);
    
    if (items.length > 0) {
        showError(`Không thể xóa danh mục "${category.name}" vì còn ${items.length} món. Vui lòng xóa các món trước.`);
        return;
    }
    
    if (!confirm(`Bạn có chắc muốn xóa danh mục "${category.name}"?`)) return;
    
    try {
        await api.deleteCategory(id);
        showSuccess('Đã xóa danh mục');
        await loadMenu();
    } catch (error) {
        showError(error.message || 'Không thể xóa danh mục');
    }
}

async function deleteMenuItem(id) {
    const item = menuItems.find(i => i.id === id);
    if (!item) return;
    
    if (!confirm(`Bạn có chắc muốn xóa món "${item.name}"?`)) return;
    
    try {
        await api.deleteMenuItem(id);
        showSuccess('Đã xóa món');
        await loadMenu();
    } catch (error) {
        showError(error.message || 'Không thể xóa món');
    }
}

// Event Listeners
document.addEventListener('DOMContentLoaded', init);
document.getElementById('categoryForm').addEventListener('submit', submitCategory);
document.getElementById('menuItemForm').addEventListener('submit', submitMenuItem);
