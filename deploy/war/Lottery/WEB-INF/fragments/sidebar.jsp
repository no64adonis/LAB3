<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div class="balance-card-widget">
    <div style="font-size: 0.9em; color: #a0a0b0; text-transform: uppercase; letter-spacing: 1px;">My Balance</div>
    <c:choose>
        <c:when test="${not empty user}">
            <div class="balance-amount">$${user.balance}</div>
        </c:when>
        <c:otherwise>
            <div class="balance-amount">$0.00</div>
        </c:otherwise>
    </c:choose>
    <a href="topup" class="btn lottery-btn" style="width: 100%; border-radius: 8px;">Quick Top Up</a>
</div>
<ul>
    <li><a href="userLottery"><i class="fas fa-search"></i> Lottery Search</a></li>
    <li><a href="ticketPurchase"><i class="fas fa-shopping-cart"></i> Purchase Tickets</a></li>
    <li><a href="myTickets"><i class="fas fa-ticket-alt"></i> My Tickets</a></li>
    <li><a href="payments"><i class="fas fa-credit-card"></i> Payment Methods</a></li>
    <li><a href="topup"><i class="fas fa-plus-circle"></i> Top Up Balance</a></li>
    <li><a href="profile"><i class="fas fa-user"></i> My Profile</a></li>
    <li class="logout-item" style="margin-top: 30px;"><a href="logout" style="color: #ff5252;"><i class="fas fa-sign-out-alt"></i> Logout</a></li>
</ul>

<div class="balance-card-widget" style="margin-top: 20px; background: rgba(33, 150, 243, 0.1); border-color: rgba(33, 150, 243, 0.3);">
    <div style="font-size: 0.8em; color: #90caf9; text-transform: uppercase; letter-spacing: 1px;">Next Draw In</div>
    <div id="draw-countdown" style="font-size: 1.5em; font-weight: 700; color: #bbdefb; margin: 5px 0;">00:00:00</div>
</div>
<script>
    function updateCountdown() {
        const now = new Date();
        const draw = new Date(now);
        
        let minutes = draw.getMinutes();
        let nextTenMinuteMark = (Math.floor(minutes / 10) + 1) * 10;
        
        if (nextTenMinuteMark === 60) {
            draw.setHours(draw.getHours() + 1);
            draw.setMinutes(0);
        } else {
            draw.setMinutes(nextTenMinuteMark);
        }
        draw.setSeconds(0);
        draw.setMilliseconds(0);

        const diff = draw - now;
        const hours = Math.floor(diff / (1000 * 60 * 60)).toString().padStart(2, '0');
        const mins = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60)).toString().padStart(2, '0');
        const seconds = Math.floor((diff % (1000 * 60)) / 1000).toString().padStart(2, '0');

        const el = document.getElementById('draw-countdown');
        if(el) el.textContent = hours + ":" + mins + ":" + seconds;
    }
    
    if (!window.countdownInterval) {
        window.countdownInterval = setInterval(updateCountdown, 1000);
    }
    updateCountdown();
</script>
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

