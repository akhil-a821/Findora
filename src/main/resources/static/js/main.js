// Theme initialization moved to <head> in base.html to prevent white flash

// Preloader Auto-Dismissal (Only runs on initial site opening, skipped during page navigation)
(function() {
    if (sessionStorage.getItem('findoraVisited')) {
        document.documentElement.classList.add('skip-preloader');
    }
})();

window.addEventListener('load', () => {
    const preloader = document.getElementById('appPreloader');
    if (preloader) {
        if (sessionStorage.getItem('findoraVisited')) {
            preloader.style.display = 'none';
        } else {
            sessionStorage.setItem('findoraVisited', 'true');
            setTimeout(() => {
                preloader.classList.add('preloader-hidden');
            }, 2000);
        }
    }
});

document.addEventListener('DOMContentLoaded', () => {
    // Theme Toggle Button Handler
    const themeToggleBtn = document.getElementById('themeToggleBtn');
    const themeIcon = document.getElementById('themeIcon');

    function updateThemeIcon(theme) {
        if (themeIcon) {
            if (theme === 'dark') {
                themeIcon.className = 'fa-solid fa-sun text-warning';
            } else {
                themeIcon.className = 'fa-solid fa-moon text-primary';
            }
        }
    }

    const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
    updateThemeIcon(currentTheme);

    if (themeToggleBtn) {
        themeToggleBtn.addEventListener('click', () => {
            const activeTheme = document.documentElement.getAttribute('data-theme');
            const newTheme = activeTheme === 'dark' ? 'light' : 'dark';
            document.documentElement.setAttribute('data-bs-theme', newTheme);
            document.documentElement.setAttribute('data-theme', newTheme);
            localStorage.setItem('findoraTheme', newTheme);
            updateThemeIcon(newTheme);
        });
    }
    // 1. Toast Notification Auto-Dismissal
    const toastElements = document.querySelectorAll('.toast');
    toastElements.forEach(toastEl => {
        setTimeout(() => {
            const bsToast = bootstrap.Toast.getInstance(toastEl) || new bootstrap.Toast(toastEl);
            bsToast.hide();
        }, 5000);
    });

    // 2. Report Type Toggle (LOST vs FOUND)
    const btnLost = document.getElementById('btnToggleLost');
    const btnFound = document.getElementById('btnToggleFound');
    const inputType = document.getElementById('reportTypeInput');
    const lostPrivateHelp = document.getElementById('lostPrivateHelp');
    const foundPrivateHelp = document.getElementById('foundPrivateHelp');

    if (btnLost && btnFound && inputType) {
        btnLost.addEventListener('click', () => {
            inputType.value = 'LOST';
            btnLost.classList.add('btn-primary-custom');
            btnLost.classList.remove('btn-outline-custom');
            btnFound.classList.add('btn-outline-custom');
            btnFound.classList.remove('btn-primary-custom');

            if (lostPrivateHelp && foundPrivateHelp) {
                lostPrivateHelp.classList.remove('d-none');
                foundPrivateHelp.classList.add('d-none');
            }
        });

        btnFound.addEventListener('click', () => {
            inputType.value = 'FOUND';
            btnFound.classList.add('btn-primary-custom');
            btnFound.classList.remove('btn-outline-custom');
            btnLost.classList.add('btn-outline-custom');
            btnLost.classList.remove('btn-primary-custom');

            if (lostPrivateHelp && foundPrivateHelp) {
                foundPrivateHelp.classList.remove('d-none');
                lostPrivateHelp.classList.add('d-none');
            }
        });
    }

    // 3. Image Upload Preview
    const imageInput = document.getElementById('imageFileInput');
    const imagePreview = document.getElementById('imagePreviewContainer');
    const imagePreviewImg = document.getElementById('imagePreviewImg');

    if (imageInput && imagePreview && imagePreviewImg) {
        imageInput.addEventListener('change', (e) => {
            const file = e.target.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = (evt) => {
                    imagePreviewImg.src = evt.target.result;
                    imagePreview.classList.remove('d-none');
                };
                reader.readAsDataURL(file);
            } else {
                imagePreview.classList.add('d-none');
            }
        });
    }

    // 4. Form Double-Submission Prevention & Loading Feedback
    const allForms = document.querySelectorAll('form');
    allForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            const submitBtn = form.querySelector('button[type="submit"]');
            if (submitBtn && !submitBtn.disabled) {
                // Don't disable if form is invalid (HTML5 validation will handle)
                if (form.checkValidity && !form.checkValidity()) {
                    return;
                }
                setTimeout(() => {
                    submitBtn.disabled = true;
                    const originalHtml = submitBtn.innerHTML;
                    submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span> Processing...';
                }, 10);
            }
        });
    });
});

// 5. AJAX Mark Notification as Read
function markNotificationAsRead(id, element) {
    fetch('/notifications/read/' + id, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    }).then(response => {
        if (response.ok) {
            if (element) {
                element.classList.remove('bg-light');
                element.classList.add('opacity-75');
            }
        }
    }).catch(err => console.error(err));
}
