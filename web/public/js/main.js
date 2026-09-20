/**
 * ファンサうちわAndroid LP
 * UI動的制御 (Vanilla JS)
 */

document.addEventListener('DOMContentLoaded', () => {
    // Hamburger menu toggle logic
    const hamburgerBtn = document.getElementById('hamburger-btn');
    const headerNav = document.getElementById('header-nav');
    const navLinks = document.querySelectorAll('.nav-link');

    if (hamburgerBtn && headerNav) {
        hamburgerBtn.addEventListener('click', () => {
            hamburgerBtn.classList.toggle('active');
            headerNav.classList.toggle('active');
        });

        // Close menu when a link is clicked
        navLinks.forEach(link => {
            link.addEventListener('click', () => {
                hamburgerBtn.classList.remove('active');
                headerNav.classList.remove('active');
            });
        });
    }
    // スクロール時のフェードインアニメーション
    const setupIntersectionObserver = () => {
        const fadeElements = document.querySelectorAll('.fade-in');

        const observerOptions = {
            root: null,
            rootMargin: '0px 0px -100px 0px', // 要素が画面下部から100px入った時点で発火
            threshold: 0.1
        };

        const observerCallback = (entries, observer) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('visible');
                    // 一度表示されたら監視を解除（再度スクロールしてもアニメーションさせない場合）
                    observer.unobserve(entry.target);
                }
            });
        };

        const observer = new IntersectionObserver(observerCallback, observerOptions);

        fadeElements.forEach(el => {
            observer.observe(el);
        });

        // ページ読み込み時に最初から画面内にあるものを即座に表示表示
        setTimeout(() => {
            fadeElements.forEach(el => {
                const rect = el.getBoundingClientRect();
                if (rect.top < window.innerHeight) {
                    el.classList.add('visible');
                }
            });
        }, 100);
    };

    setupIntersectionObserver();
});
