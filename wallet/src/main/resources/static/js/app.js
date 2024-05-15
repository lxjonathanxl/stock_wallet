document.addEventListener('DOMContentLoaded', function () {
    const passwordInput = document.querySelector('#password');
    const passwordChecklist = document.querySelectorAll('.list-item');
    const validElement = document.querySelector('#valid'); // Assuming you have an element with id="valid" in your HTML

    const validationRegex = [
        /.{8,}/,
        /[0-9]/,
        /[a-z]/,
        /[A-Z]/,
        /[^A-Za-z0-9]/
    ];

    function validatePassword() {
        const password = passwordInput.value;
        let validator = 0;
        const validations = passwordChecklist.length;

        validationRegex.forEach((regex, i) => {
            if (regex.test(password)) {
                passwordChecklist[i].classList.add('checked');
                validator++;

                if (validator === validations) {
                    validElement.value = 'true'; // Update the value of the validElement if all validations pass
                }
                else
                {
                    validElement.value = 'false';
                }
            } else {
                passwordChecklist[i].classList.remove('checked');
            }
        });
    }

    // Attach event listener
    passwordInput.addEventListener('keyup', validatePassword);
});
