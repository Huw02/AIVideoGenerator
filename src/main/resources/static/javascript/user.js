import { postObjectAsJson } from './modulejson.js';
const API_BASE = 'http://localhost:8080/api/v1';



async function createUser(event){
    console.log("creating user")
    const userPostApi = API_BASE + "/users/register"
    event.preventDefault();

    const name = document.getElementById('name').value;
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    // Create the request body
    const data = {
        name: name,
        username: username,
        password: password
    };

    try {
        let response = await postObjectAsJson(userPostApi, data, "POST");
        if(response.status === 409){ //hvis brugernavn er taget, så får man en 409
            alert("Username is already taken. Please choose another.");
        } else if(response.status === 201) {
            alert("User created successfully!");
        } else {
            alert("something else went wrong, code: " + response.status.toString())

        }
    } catch (err) {
        console.error("Error creating user:", err);
        alert("Failed to create user.");
    }
}


async function login(e) {
    e.preventDefault();

    const loginPostAPi = API_BASE + "/users/login"



    const loginAuth = {
        username: document.getElementById("loginUsername").value,
        password: document.getElementById("loginPassword").value
    }

    let response = await postObjectAsJson(loginPostAPi, loginAuth, "POST", false, true)
    console.log(response)

    if(response.status === 200){
        const token = response.headers.get("Authorization");
        console.log("JWT token:", token);


        if (token.startsWith("Bearer ")) {
            localStorage.setItem("jwt", token.substring(7)); // remove "Bearer "
        } else {
            localStorage.setItem("jwt", token);
        }

        showUserPage(); // skifter til en ny html side
    } else if(response.status === 401){
        alert("invalid username or password!")
    } else {
        const text = await response.text();
        alert("Login failed: " + text);
    }
}


function showUserPage(){
    window.location.href = "/AIVideoGeneratorBackend/static/index.html"
}

export {createUser, login}