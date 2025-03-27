function validateFileSize() {
    const file = document.getElementById("image").files[0];
    if (file && file.size > 3 * 1024 * 1024) {
        alert("File size must not exceed 3MB.");
        document.getElementById("image").value = "";
    }
}