// Modal helpers
function showModal(id) {
    const el = document.getElementById(id);
    if (el) {
        el.classList.add('open');
        // Focus first input
        setTimeout(() => {
            const first = el.querySelector('input:not([type=hidden]), select');
            if (first) first.focus();
        }, 100);
    }
}

function hideModal(id) {
    const el = document.getElementById(id);
    if (el) el.classList.remove('open');
}

// Close modal on overlay click
document.addEventListener('click', function(e) {
    if (e.target.classList.contains('modal-overlay')) {
        e.target.classList.remove('open');
    }
});

// Close on Escape
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        document.querySelectorAll('.modal-overlay.open').forEach(el => el.classList.remove('open'));
    }
});

// Auto-hide alerts after 5s
document.addEventListener('DOMContentLoaded', function() {
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.transition = 'opacity 0.5s ease';
            alert.style.opacity = '0';
            setTimeout(() => alert.remove(), 500);
        }, 5000);
    });

    // Number formatting for money inputs
    document.querySelectorAll('.money-input').forEach(input => {
        input.addEventListener('blur', function() {
            if (this.value && !isNaN(this.value)) {
                // Keep numeric value, just validate
                const val = parseFloat(this.value);
                if (val < 0) this.value = 0;
            }
        });
    });
});

function confirmForm(event, message, type = 'primary') {
    event.preventDefault();
    let form = event.target;
    if (form.tagName !== 'FORM') {
        form = form.closest('form');
    }
    
    let btnColor = type === 'danger' ? '#dc3545' : (type === 'success' ? '#198754' : '#0d6efd');
    let icon = type === 'danger' ? 'warning' : 'question';
    
    Swal.fire({
        title: 'Xác nhận',
        text: message,
        icon: icon,
        showCancelButton: true,
        confirmButtonColor: btnColor,
        cancelButtonColor: '#6c757d',
        confirmButtonText: 'Đồng ý',
        cancelButtonText: 'Hủy',
        customClass: {
            popup: 'animated fadeInDown faster'
        }
    }).then((result) => {
        if (result.isConfirmed) {
            form.submit();
        }
    });
}
