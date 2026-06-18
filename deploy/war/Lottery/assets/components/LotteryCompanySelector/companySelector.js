

class CompanySelector {
    constructor(containerId, companies, options = {}) {
        this.containerId = containerId;
        this.companies = companies || [];
        this.selectedCompanies = new Set();
        this.isDropdownOpen = false;
        this.singleSelect = options.singleSelect || false;
        this.init();
    }

    init() {
        
        this.companies.sort((a, b) => a.localeCompare(b, undefined, { sensitivity: 'base' }));

        
        this.createHTMLStructure();

        
        this.attachEventListeners();

        
        this.updateSelectedCount();
    }

    createHTMLStructure() {
        const container = document.getElementById(this.containerId);
        if (!container) return;

        container.innerHTML = `
            <div class="company-selector-wrapper">
                <div class="dropdown-container">
                    <div class="dropdown-header" role="combobox" aria-expanded="false" aria-haspopup="listbox">
                        <input type="text" class="search-input" placeholder="Search companies..." aria-label="Search companies">
                        <span class="selected-count">(0 selected)</span>
                        <span class="dropdown-arrow">▼</span>
                    </div>
                    <div class="dropdown-body" style="display: none;">
                        ${this.singleSelect ? '' : `
                        <div class="bulk-actions">
                            <button type="button" class="btn lottery-btn select-all-btn" aria-label="Select all visible companies">Select All</button>
                            <button type="button" class="btn lottery-btn deselect-all-btn" aria-label="Deselect all visible companies">Deselect All</button>
                        </div>
                        `}
                        <div class="company-list" role="listbox">
                            ${this.companies.map(company => `
                                <div class="company-item" role="option" aria-selected="false">
                                    <input type="checkbox" id="${this.containerId}_company_${this.sanitizeId(company)}" value="${company}" class="company-checkbox">
                                    <label for="${this.containerId}_company_${this.sanitizeId(company)}">${company}</label>
                                </div>
                            `).join('')}
                        </div>
                    </div>
                </div>
                <div class="selected-companies-panel">
                    <div class="selected-tags-container"></div>
                    <button type="button" class="btn lottery-btn clear-all-btn" style="display: none;">Clear All</button>
                </div>
            </div>
        `;
    }

    sanitizeId(str) {
        return str.replace(/[^a-zA-Z0-9]/g, '_');
    }

    attachEventListeners() {
        const container = document.getElementById(this.containerId);
        if (!container) return;

        
        const dropdownHeader = container.querySelector('.dropdown-header');
        dropdownHeader.addEventListener('click', (e) => {
            
            if (!e.target.classList.contains('search-input')) {
                this.toggleDropdown();
            }
        });

        
        const searchInput = container.querySelector('.search-input');
        let searchTimeout;
        searchInput.addEventListener('input', (e) => {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                this.filterCompanies(e.target.value);
            }, 200);
        });

        
        searchInput.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                this.closeDropdown();
            }
        });

        
        const companyList = container.querySelector('.company-list');
        companyList.addEventListener('change', (e) => {
            if (e.target.classList.contains('company-checkbox')) {
                this.toggleCompanySelection(e.target.value, e.target.checked);
            }
        });

        
        const selectAllBtn = container.querySelector('.select-all-btn');
        if (selectAllBtn) {
            selectAllBtn.addEventListener('click', () => {
                this.selectAll();
            });
        }

        
        const deselectAllBtn = container.querySelector('.deselect-all-btn');
        if (deselectAllBtn) {
            deselectAllBtn.addEventListener('click', () => {
                this.deselectAll();
            });
        }

        
        const clearAllBtn = container.querySelector('.clear-all-btn');
        if (clearAllBtn) {
            clearAllBtn.addEventListener('click', () => {
                this.clearAllSelections();
            });
        }

        
        document.addEventListener('click', (e) => {
            if (!container.contains(e.target) && this.isDropdownOpen) {
                this.closeDropdown();
            }
        });

        
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && this.isDropdownOpen) {
                this.closeDropdown();
            }
        });
    }

    setOpenStateStyling(isOpen) {
        const container = document.getElementById(this.containerId);
        if (!container) return;

        const wrapper = container.querySelector('.company-selector-wrapper');
        if (wrapper) {
            wrapper.classList.toggle('company-selector-open', isOpen);
        }

        const parentSurface = container.closest('.search-box, .create-form, .form-container');
        if (parentSurface) {
            parentSurface.classList.toggle('company-selector-open', isOpen);
        }
    }

    toggleDropdown() {
        const container = document.getElementById(this.containerId);
        if (!container) return;

        const dropdownBody = container.querySelector('.dropdown-body');
        const dropdownHeader = container.querySelector('.dropdown-header');
        const searchInput = container.querySelector('.search-input');
        const arrow = container.querySelector('.dropdown-arrow');

        this.isDropdownOpen = !this.isDropdownOpen;

        if (this.isDropdownOpen) {
            dropdownBody.style.display = 'block';
            dropdownHeader.setAttribute('aria-expanded', 'true');
            arrow.textContent = '▲';
            searchInput.focus();
        } else {
            dropdownBody.style.display = 'none';
            dropdownHeader.setAttribute('aria-expanded', 'false');
            arrow.textContent = '▼';
        }
        this.setOpenStateStyling(this.isDropdownOpen);
    }

    closeDropdown() {
        const container = document.getElementById(this.containerId);
        if (!container) return;

        const dropdownBody = container.querySelector('.dropdown-body');
        const dropdownHeader = container.querySelector('.dropdown-header');
        const arrow = container.querySelector('.dropdown-arrow');

        this.isDropdownOpen = false;
        dropdownBody.style.display = 'none';
        dropdownHeader.setAttribute('aria-expanded', 'false');
        arrow.textContent = '▼';
        this.setOpenStateStyling(false);
    }

    filterCompanies(searchTerm) {
        const container = document.getElementById(this.containerId);
        if (!container) return;

        const companyItems = container.querySelectorAll('.company-item');
        const searchTermLower = searchTerm.toLowerCase();

        companyItems.forEach(item => {
            const companyName = item.querySelector('label').textContent.toLowerCase();
            if (companyName.includes(searchTermLower)) {
                item.style.display = 'flex';
            } else {
                item.style.display = 'none';
            }
        });
    }

    toggleCompanySelection(company, isChecked) {
        const container = document.getElementById(this.containerId);
        if (!container) return;

        if (this.singleSelect) {
            if (isChecked) {
                
                this.selectedCompanies.clear();
                const tagsContainer = container.querySelector('.selected-tags-container');
                if (tagsContainer) tagsContainer.innerHTML = '';

                const checkboxes = container.querySelectorAll('.company-checkbox');
                checkboxes.forEach(cb => {
                    if (cb.value !== company) {
                        cb.checked = false;
                    }
                });

                this.selectedCompanies.add(company);
                this.addTag(company);
            } else {
                
                this.selectedCompanies.delete(company);
                this.removeTag(company);
            }
        } else {
            
            if (isChecked) {
                this.selectedCompanies.add(company);
                this.addTag(company);
            } else {
                this.selectedCompanies.delete(company);
                this.removeTag(company);
            }
        }

        
        const checkbox = container.querySelector(`#${this.containerId}_company_${this.sanitizeId(company)}`);
        if (checkbox) {
            checkbox.checked = isChecked;
        }

        this.updateSelectedCount();
        this.updateClearAllButton();
    }

    selectAll() {
        const container = document.getElementById(this.containerId);
        if (!container) return;

        const visibleCheckboxes = container.querySelectorAll('.company-item:not([style*="display: none"]) .company-checkbox');
        visibleCheckboxes.forEach(checkbox => {
            if (!checkbox.checked) {
                checkbox.checked = true;
                this.selectedCompanies.add(checkbox.value);
                this.addTag(checkbox.value);
            }
        });

        this.updateSelectedCount();
        this.updateClearAllButton();
    }

    deselectAll() {
        const container = document.getElementById(this.containerId);
        if (!container) return;

        const visibleCheckboxes = container.querySelectorAll('.company-item:not([style*="display: none"]) .company-checkbox');
        visibleCheckboxes.forEach(checkbox => {
            if (checkbox.checked) {
                checkbox.checked = false;
                this.selectedCompanies.delete(checkbox.value);
                this.removeTag(checkbox.value);
            }
        });

        this.updateSelectedCount();
        this.updateClearAllButton();
    }

    addTag(company) {
        const container = document.getElementById(this.containerId);
        if (!container) return;

        const tagsContainer = container.querySelector('.selected-tags-container');
        const tagExists = tagsContainer.querySelector(`.tag[data-company="${company}"]`);

        if (tagExists) return;

        const tag = document.createElement('div');
        tag.className = 'tag';
        tag.setAttribute('data-company', company);
        tag.innerHTML = `
            <button type="button" class="tag-remove-btn" aria-label="Remove ${company}">×</button>
            <span class="tag-text">${this.truncateText(company, 20)}</span>
        `;

        tag.querySelector('.tag-remove-btn').addEventListener('click', () => {
            this.removeCompanyTag(company);
        });

        tagsContainer.appendChild(tag);
    }

    removeTag(company) {
        const container = document.getElementById(this.containerId);
        if (!container) return;

        const tag = container.querySelector(`.tag[data-company="${company}"]`);
        if (tag) {
            tag.remove();
        }
    }

    removeCompanyTag(company) {
        this.selectedCompanies.delete(company);
        this.removeTag(company);

        
        const container = document.getElementById(this.containerId);
        if (container) {
            const checkbox = container.querySelector(`#${this.containerId}_company_${this.sanitizeId(company)}`);
            if (checkbox) {
                checkbox.checked = false;
            }
        }

        this.updateSelectedCount();
        this.updateClearAllButton();
    }

    clearAllSelections() {
        
        this.selectedCompanies.clear();

        const container = document.getElementById(this.containerId);
        if (container) {
            const tags = container.querySelectorAll('.tag');
            tags.forEach(tag => tag.remove());

            
            const checkboxes = container.querySelectorAll('.company-checkbox');
            checkboxes.forEach(checkbox => {
                checkbox.checked = false;
            });
        }

        this.updateSelectedCount();
        this.updateClearAllButton();
    }

    updateSelectedCount() {
        const container = document.getElementById(this.containerId);
        if (!container) return;

        const countElement = container.querySelector('.selected-count');
        if (countElement) {
            if (this.singleSelect) {
                countElement.textContent = this.selectedCompanies.size > 0 ? '(1 selected)' : '(0 selected)';
            } else {
                countElement.textContent = `(${this.selectedCompanies.size} selected)`;
            }
        }
    }

    updateClearAllButton() {
        const container = document.getElementById(this.containerId);
        if (!container) return;

        const clearAllBtn = container.querySelector('.clear-all-btn');
        if (clearAllBtn) {
            clearAllBtn.style.display = this.selectedCompanies.size > 0 ? 'inline-block' : 'none';
        }
    }

    truncateText(text, maxLength) {
        if (text.length <= maxLength) return text;
        return text.substring(0, maxLength - 3) + '...';
    }

    getSelectedCompanies() {
        return Array.from(this.selectedCompanies);
    }

    setSelectedCompanies(companies) {
        this.selectedCompanies = new Set(companies);

        
        const container = document.getElementById(this.containerId);
        if (!container) return;

        
        const tagsContainer = container.querySelector('.selected-tags-container');
        if (tagsContainer) {
            tagsContainer.innerHTML = '';
        }

        
        this.companies.forEach(company => {
            const checkbox = container.querySelector(`#${this.containerId}_company_${this.sanitizeId(company)}`);
            if (checkbox) {
                const isSelected = this.selectedCompanies.has(company);
                checkbox.checked = isSelected;
                if (isSelected) {
                    this.addTag(company);
                }
            }
        });

        this.updateSelectedCount();
        this.updateClearAllButton();
    }
}

