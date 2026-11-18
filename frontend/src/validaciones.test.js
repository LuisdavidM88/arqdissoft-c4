import { describe, expect, it } from 'vitest';
import { normalizarPlaca, validarCedula, validarCorreo, validarPlaca, validarRuc } from './validaciones.js';

describe('validaciones', () => {
  it('acepta cédula y RUC de persona natural válidos', () => {
    expect(validarCedula('1700000001')).toBeNull();
    expect(validarRuc('1700000001001')).toBeNull();
  });

  it('rechaza dígito verificador incorrecto con un mensaje claro', () => {
    expect(validarCedula('1710034066')).toMatch(/verificador/);
    expect(validarRuc('1710034066001')).toMatch(/cédula válida/);
  });

  it('deja pasar RUC de sociedad para que el SRI confirme el tipo', () => {
    expect(validarRuc('1760013210001')).toBeNull();
    expect(validarRuc('1790000001001')).toBeNull();
  });

  it('valida longitud y formato', () => {
    expect(validarRuc('170000000100')).toMatch(/13 dígitos/);
    expect(validarCorreo('usuario@gmail')).toMatch(/formato/);
    expect(validarCorreo('usuario@gmail.com')).toBeNull();
    expect(validarCorreo('maria.perez@hotmail.com')).toBeNull();
  });

  it('normaliza y valida la placa', () => {
    expect(normalizarPlaca(' pba-1234 ')).toBe('PBA1234');
    expect(validarPlaca('PBA1234')).toBeNull();
    expect(validarPlaca('AB123C')).toBeNull();
    expect(validarPlaca('12ABC')).toMatch(/formato/);
  });
});
