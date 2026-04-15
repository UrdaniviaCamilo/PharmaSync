document.getElementById("buscarMedicamento").addEventListener("input", function () {
    let q = this.value;

    fetch("/buscar?q=" + q)
        .then(res => res.json())
        .then(data => {
            let lista = document.getElementById("sugerencias");
            lista.innerHTML = "";

            data.forEach(p => {
                let li = document.createElement("li");
                li.textContent = p.nombre;
                lista.appendChild(li);
            });
        });
});