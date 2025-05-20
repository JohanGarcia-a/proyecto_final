package model;

public class LineaVenta {
	private int id;
	private int idVenta;
	private final Producto producto;
	private int cantidad;
	private final double precio;


	public LineaVenta(Producto producto, int cantidad) {
		this.producto = producto;
		this.cantidad = cantidad;
		this.precio = producto.getPrecio();
	}


	public void agregarCantidad(int mas) {
		this.cantidad += mas;
	}


	public void fijarCantidad(int cantidad) {
		this.cantidad = cantidad;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getIdVenta() {
		return idVenta;
	}

	public void setIdVenta(int idVenta) {
		this.idVenta = idVenta;
	}

	public Producto getProducto() {
		return producto;
	}

	public int getCantidad() {
		return cantidad;
	}

	public double getPrecio() {
		return precio;
	}
}
