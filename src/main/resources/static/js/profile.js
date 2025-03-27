function validateFileSize() {
    const file = document.getElementById("profilePicture").files[0];
    if (file && file.size > 3 * 1024 * 1024) {
        alert("File size must not exceed 3MB.");
        document.getElementById("profilePicture").value = "";
    }
}
