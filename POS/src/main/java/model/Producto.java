package model;

public class Producto {
    private int id;
    private String nombre;
    private double precio;
    private int existencias;
    private String categoria;
    private String marca;
    private String tamano;
    private String rutaImagen;

    public Producto() {
        this.rutaImagen = "";
    }

    public Producto(int id, String nombre, double precio, int existencias, String categoria, String marca,
                    String tamano, String rutaImagen) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.existencias = existencias;
        this.categoria = categoria;
        this.marca = marca;
        this.tamano = tamano;
        this.rutaImagen = rutaImagen;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }


    public double getPrecio() {
        return precio;
    }


    public int getExistencias() {
        return existencias;
    }


    public String getCategoria() {
        return categoria;
    }


    public String getMarca() {
        return marca;
    }


    public String getTamano() {
        return tamano;
    }


    public String getRutaImagen() {
        return rutaImagen;
    }


    @Override
    public String toString() {
        return nombre;
    }

}
