

function toggleCustomSelect(element, event) {
    if (event) event.stopPropagation();
    
    const container = element.closest('.custom-select-container');
    const list = container.querySelector('.custom-select-list');
    
    
    document.querySelectorAll('.custom-select-header.active').forEach(h => {
        if (h !== element) {
            h.classList.remove('active');
            const siblingList = h.closest('.custom-select-container').querySelector('.custom-select-list');
            if (siblingList) siblingList.classList.remove('show');
        }
    });

    element.classList.toggle('active');
    if (list) list.classList.toggle('show');
}

function selectCustomItem(element, event) {
    if (event) event.stopPropagation();

    const container = element.closest('.custom-select-container');
    const header = container.querySelector('.custom-select-header');
    const textSpan = container.querySelector('.custom-select-text');
    const list = container.querySelector('.custom-select-list');
    const hiddenInput = container.querySelector('input[type="hidden"]');
    
    const newValue = element.getAttribute('data-value');
    const newText = element.textContent.trim();

    
    if (textSpan) {
        textSpan.textContent = newText;
    }

    
    if (hiddenInput) {
        hiddenInput.value = newValue;
        
        
        const changeEvent = new Event('change', { bubbles: true });
        hiddenInput.dispatchEvent(changeEvent);
        
        
        const onchangeScript = container.getAttribute('data-onchange');
        if (onchangeScript) {
            new Function(onchangeScript).call(hiddenInput);
        }
    }

    
    if (header) header.classList.remove('active');
    if (list) list.classList.remove('show');
    
    
    container.querySelectorAll('.custom-select-item').forEach(item => {
        item.classList.remove('selected');
        
    });
    element.classList.add('selected');
}


document.addEventListener('click', function (event) {
    if (!event.target.closest('.custom-select-container')) {
        document.querySelectorAll('.custom-select-header.active').forEach(h => {
            h.classList.remove('active');
            const container = h.closest('.custom-select-container');
            if (container) {
                const list = container.querySelector('.custom-select-list');
                if (list) list.classList.remove('show');
            }
        });
    }
});


document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.custom-select-container').forEach(container => {
        const hiddenInput = container.querySelector('input[type="hidden"]');
        if (hiddenInput && hiddenInput.value) {
            const selectedItem = container.querySelector('.custom-select-item[data-value="' + hiddenInput.value + '"]');
            if (selectedItem) {
                const textSpan = container.querySelector('.custom-select-text');
                if (textSpan) {
                    textSpan.textContent = selectedItem.textContent.trim();
                }
                selectedItem.classList.add('selected');
            }
        }
    });
});
