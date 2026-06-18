<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<h2><i class="fas fa-user-shield"></i> Admin Dashboard</h2>
<ul>
    <li><a href="userManagement"><i class="fas fa-users"></i> User Management</a></li>
    <li><a href="paymentMethodManagement"><i class="fas fa-credit-card"></i> Payment Methods</a></li>
    <li><a href="adminLottery"><i class="fas fa-ticket-alt"></i> Lottery Management</a></li>
    <li><a href="adminTransactions"><i class="fas fa-exchange-alt"></i> Transactions Management</a></li>
    <li><a href="adminProfile"><i class="fas fa-user"></i> My Profile</a></li>
    <li class="logout-item" style="margin-top: 30px;"><a href="logout" style="color: #ff5252;"><i class="fas fa-sign-out-alt"></i> Logout</a></li>
</ul>
<script>
    
    (function() {
        if (document.getElementById('mobile-sidebar-init')) return;
        var marker = document.createElement('meta');
        marker.id = 'mobile-sidebar-init';
        document.head.appendChild(marker);

        document.addEventListener('DOMContentLoaded', function() {
            var sidebar = document.querySelector('.sidebar');
            if (!sidebar) return;

            var isMobile = function() { return window.innerWidth <= 768; };
            var sidebarOpen = false;
            function setImp(p, v) { sidebar.style.setProperty(p, v, 'important'); }
            
            function applyMobileStyles() {
                if (isMobile()) {
                    setImp('position', 'fixed');
                    setImp('top', '0');
                    setImp('left', sidebarOpen ? '0' : '-280px');
                    setImp('width', '260px');
                    setImp('height', '100vh');
                    setImp('z-index', '9999');
                    setImp('background-color', '#1a1a2e');
                    setImp('border-right', '1px solid #3a3f5a');
                    setImp('padding', '20px');
                    setImp('transition', 'left 0.3s ease');
                    setImp('overflow-y', 'auto');
                    setImp('flex-shrink', 'unset');
                    if (sidebarOpen) {
                        setImp('box-shadow', '5px 0 25px rgba(0,0,0,0.5)');
                    } else {
                        setImp('box-shadow', 'none');
                    }
                    closeBtn.style.setProperty('display', 'flex', 'important');
                    toggle.style.setProperty('display', 'flex', 'important');
                } else {
                    
                    var props = ['position', 'top', 'left', 'width', 'height', 'z-index', 'background-color', 'border-right', 'padding', 'transition', 'overflow-y', 'flex-shrink', 'box-shadow'];
                    props.forEach(function(p) { sidebar.style.removeProperty(p); });
                    closeBtn.style.setProperty('display', 'none', 'important');
                    toggle.style.setProperty('display', 'none', 'important');
                    sidebarOpen = false;
                    backdrop.classList.remove('active');
                }
            }

            
            var closeBtn = document.createElement('button');
            closeBtn.setAttribute('data-mobile-nav', 'true');
            closeBtn.innerHTML = '&times;';
            closeBtn.style.cssText = 'display:none;position:absolute;top:10px;right:10px;width:32px;height:32px;background:transparent;border:1px solid #3a3f5a;border-radius:6px;color:#ff5252;font-size:18px;cursor:pointer;align-items:center;justify-content:center;z-index:10000;padding:0;';
            closeBtn.addEventListener('click', function(e) {
                e.stopPropagation();
                closeSidebar();
            });
            sidebar.insertBefore(closeBtn, sidebar.firstChild);

            
            var backdrop = document.createElement('div');
            backdrop.className = 'mobile-sidebar-backdrop';
            backdrop.addEventListener('click', closeSidebar);
            document.body.appendChild(backdrop);

            
            var toggle = document.createElement('button');
            toggle.className = 'mobile-menu-toggle';
            toggle.setAttribute('data-mobile-nav', 'true');
            toggle.innerHTML = '<i class="fas fa-bars"></i>';
            toggle.addEventListener('click', function(e) {
                e.stopPropagation();
                openSidebar();
            });
            var header = document.querySelector('.header');
            if (header) {
                header.insertBefore(toggle, header.firstChild);
            }

            function openSidebar() {
                sidebarOpen = true;
                sidebar.style.setProperty('left', '0', 'important');
                sidebar.style.setProperty('box-shadow', '5px 0 25px rgba(0,0,0,0.5)', 'important');
                sidebar.classList.add('mobile-open');
                backdrop.classList.add('active');
            }

            function closeSidebar() {
                sidebarOpen = false;
                sidebar.style.setProperty('left', '-280px', 'important');
                sidebar.style.setProperty('box-shadow', 'none', 'important');
                sidebar.classList.remove('mobile-open');
                backdrop.classList.remove('active');
            }

            
            setTimeout(function() {
                var mobileNavBtns = document.querySelectorAll('[data-mobile-nav]');
                mobileNavBtns.forEach(function(btn) {
                    btn.classList.remove('lottery-btn');
                });
            }, 200);

            
            applyMobileStyles();
            window.addEventListener('resize', applyMobileStyles);
        });
    })();
</script>

