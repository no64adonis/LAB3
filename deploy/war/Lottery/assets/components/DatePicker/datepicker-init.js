

document.addEventListener('DOMContentLoaded', function () {
    
    const flatpickrConfig = {
        dateFormat: "Y-m-d", 
        allowInput: true,    
        disableMobile: "true", 
        locale: {
            firstDayOfWeek: 1 
        }
    };

    
    
    const dateInputs = document.querySelectorAll('.date-picker');

    if (dateInputs.length > 0) {
        flatpickr('.date-picker', flatpickrConfig);
    }

    
    
});
