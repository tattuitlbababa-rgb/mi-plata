<script>
    // Mismas claves que el resto de las páginas
    const CLAVE_MOVIMIENTOS = 'miPlata_movimientos';
    const CLAVE_CATEGORIAS = 'miPlata_categorias';

    // Categorías predefinidas, iguales a las de ingresar.html y gastos.html
    const PREDEFINIDAS = {
      Ingreso: ['Trabajo', 'Ventas', 'Regalo', 'Otros'],
      Gasto: ['Alimentación', 'Transporte', 'Servicios', 'Salud', 'Entretenimiento', 'Otros']
    };

    const form = document.getElementById('form-categoria');
    const mensaje = document.getElementById('mensaje');
    const nombre = document.getElementById('nombre');
    const tipo = document.getElementById('tipo');

    function leerMovimientos() {
      try {
        return JSON.parse(localStorage.getItem(CLAVE_MOVIMIENTOS)) || [];
      } catch (error) {
        return [];
      }
    }

    function leerCustom() {
      try {
        return JSON.parse(localStorage.getItem(CLAVE_CATEGORIAS)) || [];
      } catch (error) {
        return [];
      }
    }

    function guardarCustom(lista) {
      localStorage.setItem(CLAVE_CATEGORIAS, JSON.stringify(lista));
    }

    function formatearMonto(numero) {
      return '$ ' + Number(numero).toLocaleString('es-AR', {
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
      });
    }

    // Quita mayúsculas y acentos para comparar nombres
    function normalizar(texto) {
      return String(texto).toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '').trim();
    }

    // Escapa caracteres peligrosos, incluidas comillas (para usarlo también en atributos)
    function escapar(texto) {
      return String(texto)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
    }

    // Arma la tabla de un tipo (Ingreso o Gasto)
    function dibujarTipo(tipoElegido, cuerpoId) {
      const movimientos = leerMovimientos();
      const custom = leerCustom();

      // Une predefinidas, propias y las que aparecen en movimientos, sin repetir
      const nombres = new Set(PREDEFINIDAS[tipoElegido]);
      custom.filter(c => c.tipo === tipoElegido).forEach(c => nombres.add(c.nombre));
      movimientos.filter(m => m.tipo === tipoElegido).forEach(m => nombres.add(m.categoria));

      const filas = [...nombres].map(nombreCat => {
        const delTipo = movimientos.filter(m => m.tipo === tipoElegido && m.categoria === nombreCat);
        const total = delTipo.reduce((suma, m) => suma + Number(m.importe), 0);
        const esPropia = custom.some(c => c.tipo === tipoElegido && c.nombre === nombreCat);
        return { nombre: nombreCat, cantidad: delTipo.length, total, esPropia };
      }).sort((a, b) => b.total - a.total || a.nombre.localeCompare(b.nombre));

      document.getElementById(cuerpoId).innerHTML = filas.map(f => `
        <tr>
          <td>${escapar(f.nombre)}</td>
          <td>${f.cantidad}</td>
          <td class="importe">${formatearMonto(f.total)}</td>
          <td class="accion">
            ${f.esPropia && f.cantidad === 0
              ? `<button class="btn-eliminar" data-nombre="${escapar(f.nombre)}" data-tipo="${tipoElegido}">Eliminar</button>`
              : ''}
          </td>
        </tr>
      `).join('');
    }

    function dibujarTodo() {
      dibujarTipo('Ingreso', 'cuerpo-ingresos');
      dibujarTipo('Gasto', 'cuerpo-gastos');
    }

    // Validación del nombre
    function validarNombre() {
      const valor = nombre.value.trim();
      if (valor === '') return 'Escribí un nombre para la categoría.';
      if (valor.length < 2) return 'El nombre tiene que tener al menos 2 letras.';

      const tipoElegido = tipo.value;
      const existe = [
        ...PREDEFINIDAS[tipoElegido],
        ...leerCustom().filter(c => c.tipo === tipoElegido).map(c => c.nombre)
      ].some(n => normalizar(n) === normalizar(valor));

      if (existe) return 'Esa categoría ya existe para este tipo.';
      return '';
    }

    function mostrarError(idCampo, idError, msg) {
      document.getElementById(idError).textContent = msg;
      document.getElementById(idCampo).classList.toggle('invalido', msg !== '');
    }

    nombre.addEventListener('blur', () => mostrarError('campo-nombre', 'error-nombre', validarNombre()));

    tipo.addEventListener('change', () => {
      if (document.getElementById('campo-nombre').classList.contains('invalido')) {
        mostrarError('campo-nombre', 'error-nombre', validarNombre());
      }
    });

    form.addEventListener('submit', e => {
      e.preventDefault();
      mensaje.textContent = '';
      mensaje.className = 'mensaje';

      const errorNombre = validarNombre();
      mostrarError('campo-nombre', 'error-nombre', errorNombre);

      if (errorNombre) {
        mensaje.classList.add('error');
        mensaje.textContent = 'Revisá los campos marcados.';
        return;
      }

      const custom = leerCustom();
      custom.push({ nombre: nombre.value.trim(), tipo: tipo.value });
      guardarCustom(custom);

      form.reset();
      mensaje.classList.add('ok');
      mensaje.textContent = 'Categoría agregada';
      dibujarTodo();
    });

    // Eliminar categorías propias sin movimientos
    document.addEventListener('click', e => {
      const boton = e.target.closest('.btn-eliminar');
      if (!boton) return;

      const nombreEliminar = boton.dataset.nombre;
      const tipoEliminar = boton.dataset.tipo;
      const custom = leerCustom().filter(c => !(c.nombre === nombreEliminar && c.tipo === tipoEliminar));
      guardarCustom(custom);
      dibujarTodo();
    });

    window.addEventListener('pageshow', dibujarTodo);
    window.addEventListener('storage', e => {
      if (e.key === CLAVE_MOVIMIENTOS || e.key === CLAVE_CATEGORIAS) dibujarTodo();
    });

    dibujarTodo();
  </script>