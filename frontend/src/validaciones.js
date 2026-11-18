const PROVINCIA_VALIDA = (n) => (n >= 1 && n <= 24) || n === 30;

export const normalizarNumero = (v) => (v ?? '').replace(/[\s-]/g, '');
export const normalizarPlaca = (v) => (v ?? '').replace(/[\s-]/g, '').toUpperCase();

export function validarCorreo(correo) {
  const valor = (correo ?? '').trim();
  if (!valor) return 'Ingresa tu correo electrónico';
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(valor)) return 'El correo debe tener el formato nombre@dominio.com';
  return null;
}

export function validarCedula(cedula) {
  if (!/^\d{10}$/.test(cedula)) return 'La cédula debe tener 10 dígitos';
  if (!PROVINCIA_VALIDA(Number(cedula.slice(0, 2)))) return 'Los dos primeros dígitos no corresponden a una provincia';
  if (Number(cedula[2]) >= 6) return 'El tercer dígito de una cédula debe ser menor que 6';
  let suma = 0;
  for (let i = 0; i < 9; i++) {
    const producto = Number(cedula[i]) * (i % 2 === 0 ? 2 : 1);
    suma += producto > 9 ? producto - 9 : producto;
  }
  const verificador = (10 - (suma % 10)) % 10;
  if (verificador !== Number(cedula[9])) return 'El dígito verificador no es correcto; revisa el número';
  return null;
}

function modulo11(numero, coeficientes) {
  const suma = coeficientes.reduce((acc, c, i) => acc + Number(numero[i]) * c, 0);
  const residuo = suma % 11;
  const verificador = residuo === 0 ? 0 : 11 - residuo;
  return verificador < 10 && verificador === Number(numero[coeficientes.length]);
}

export function validarRuc(ruc) {
  if (!ruc) return 'Ingresa el RUC';
  if (!/^\d{13}$/.test(ruc)) return 'El RUC debe tener 13 dígitos (cédula + 001)';
  if (!PROVINCIA_VALIDA(Number(ruc.slice(0, 2)))) return 'Los dos primeros dígitos no corresponden a una provincia';
  const tercero = Number(ruc[2]);
  if (tercero < 6) {
    const errorCedula = validarCedula(ruc.slice(0, 10));
    if (errorCedula) return `Los 10 primeros dígitos no son una cédula válida: ${errorCedula.toLowerCase()}`;
    return ruc.endsWith('000') ? 'Los tres últimos dígitos no pueden ser 000' : null;
  }
  if (tercero === 6) {
    return modulo11(ruc, [3, 2, 7, 6, 5, 4, 3, 2]) ? null : 'El dígito verificador del RUC no es correcto';
  }
  if (tercero === 9) {
    return modulo11(ruc, [4, 3, 2, 7, 6, 5, 4, 3, 2]) ? null : 'El dígito verificador del RUC no es correcto';
  }
  return 'El tercer dígito del RUC no es válido';
}

export function validarPlaca(placa) {
  if (!placa) return 'Ingresa la placa del vehículo';
  if (/^[A-Z]{3}\d{3,4}$/.test(placa) || /^[A-Z]{2}\d{3}[A-Z]$/.test(placa)) return null;
  return 'La placa debe tener el formato ABC1234 (auto) o AB123C (moto)';
}
