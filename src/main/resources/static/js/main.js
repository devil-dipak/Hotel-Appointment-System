const API_BASE = '/api';

(function() {
    const userData = localStorage.getItem('user');
    if (!userData) {
        window.location.href = '/';
        return;
    }
})();

async function fetchJSON(url, options = {}) {
    const res = await fetch(url, {
        headers: { 'Content-Type': 'application/json', ...options.headers },
        ...options
    });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return res.json();
}

function showAlert(message, type = 'danger') {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show position-fixed top-0 start-50 translate-middle-x mt-3`;
    alertDiv.style.zIndex = '9999';
    alertDiv.innerHTML = `${message}<button type="button" class="btn-close" data-bs-dismiss="alert"></button>`;
    document.body.appendChild(alertDiv);
    setTimeout(() => alertDiv.remove(), 4000);
}

function updateNavbar() {
    const userData = localStorage.getItem('user');
    const userDropdown = document.getElementById('userDropdown');
    const loginLink = document.getElementById('loginLink');
    if (userData && userDropdown && loginLink) {
        const user = JSON.parse(userData);
        document.getElementById('userNameLink').textContent = user.name || user.email;
        userDropdown.style.display = 'block';
        loginLink.style.display = 'none';
    }
}

function logout() {
    localStorage.removeItem('user');
    window.location.href = '/';
}

document.addEventListener('DOMContentLoaded', updateNavbar);