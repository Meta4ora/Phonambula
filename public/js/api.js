function loadEmployees() {
  fetch("http://localhost:58080/api/users")
  .then(res => res.json())
  .then(data => console.log(data));
}