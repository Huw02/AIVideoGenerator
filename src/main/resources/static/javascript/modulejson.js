function fetchAnyUrl(url) {
    return fetch(url).then(response => response.json()).catch(error => console.error("Handled error xx: ", error));
}

async function postObjectAsJson(url, object, HttpVerbum) {
    const objectAsJson = JSON.stringify(object);
    const fetchOption = {
        method: HttpVerbum,
        headers: {
            "Content-Type": "Application/json"
        },
        body: objectAsJson
    }
    const response = await fetch(url, fetchOption)
    return response;
}

export {postObjectAsJson, fetchAnyUrl}