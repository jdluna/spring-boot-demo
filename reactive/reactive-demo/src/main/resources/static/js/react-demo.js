var url = 'http://localhost:8080/bank-item/reactive'; 

var refreshButton = document.getElementById('fetch');

var refreshClickStream = Rx.Observable.fromEvent(refreshButton, 'click');

var eventSourceObserver = function (observer) {
    var eventSource = new EventSource(url);
    eventSource.onmessage = function(event) {
	observer.next(event.data);
    };
    eventSource.onerror = function() {
	console.log("EventSource failed: closing the connection");
	eventSource.close();
	observer.complete();
    };
};

refreshClickStream.flatMap(event => {
    return Rx.Observable.create(eventSourceObserver);
}).map(dataAsText => {
    return JSON.parse(dataAsText);
}).subscribe(jsonData => {
    addRow(jsonData);
});

function addRow(newValueAsJson) {
    var ul = document.getElementById("items");
    var li = document.createElement("li");
    li.appendChild(document.createTextNode(newValueAsJson.id));
    ul.appendChild(li);
}
