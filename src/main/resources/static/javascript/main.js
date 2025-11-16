//import {generateVideo} from "./veo.js";
import { postObjectAsJson } from './modulejson.js';
import {createUser, login} from "./user.js";

const API_BASE = 'http://localhost:8080/api/v1';



let signUp, loginBtn, loginModal, closeModal, loginForm;

document.addEventListener('DOMContentLoaded', () => {
    signUp = document.getElementById("createUserForm")
    loginBtn = document.getElementById("loginBtn");
    loginModal = document.getElementById("loginModal");
    closeModal = document.getElementById("closeModal");
    loginForm = document.getElementById("loginForm");




    signUp.addEventListener("submit", createUser);
    loginForm.addEventListener("submit", login);
    loginBtn.addEventListener("click", () => {
        loginModal.style.display = "block";
    });
    // Close modal
    closeModal.addEventListener("click", () => {
        loginModal.style.display = "none";
    });

    window.addEventListener("click", (e) => {
        if (e.target === loginModal) loginModal.style.display = "none";
    });
})




