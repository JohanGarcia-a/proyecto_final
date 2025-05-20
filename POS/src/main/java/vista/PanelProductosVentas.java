package vista;

import model.ModeloTablaVenta;
import model.Producto;

import javax.swing.*;
import javax.swing.ScrollPaneConstants;
import java.awt.*;
import java.util.List;

import controlador.WrapLayout;

public class PanelProductosVentas extends JPanel {
    public final JTextField txtBusqueda = new JTextField(15);
    public final JButton btnBuscar = new JButton("Buscar");
    public final JComboBox<Producto> cbBuscador = new JComboBox<>();
    public final JSpinner spnCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
    public final JButton btnAgregar = new JButton("Agregar");
    public final JButton btnMas = new JButton("+");
    public final JButton btnMenos = new JButton("-");
    public final JButton btnEliminar = new JButton("Eliminar");
    public final JButton btnFinalizar = new JButton("Finalizar Venta");
    public final JLabel lblSubtotal = new JLabel("Subtotal: $0.00");
    public final JLabel lblIva = new JLabel("IVA 16%: $0.00");
    public final JLabel lblTotal = new JLabel("Total: $0.00");
    public final JLabel lblPago = new JLabel("Pago con: ");
    public final JTextField txtPago = new JTextField(7);
    public final JLabel lblCambio = new JLabel("Cambio: $0.00");
    public final JTable tblVenta = new JTable(new ModeloTablaVenta());
    public final JPanel panelMiniaturas = new JPanel();
    public final JPanel panelCategorias = new JPanel();


    public interface CategoriaListener {
        void filtrar(String categoria);
    }

    private CategoriaListener listenerCategoria;

    public void setCategoriaListener(CategoriaListener listener) {
        this.listenerCategoria = listener;
    }

    public PanelProductosVentas() {
        setLayout(new BorderLayout(5, 5));

        // Panel superior de búsqueda y acciones
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(new Color(255, 222, 173));
        top.add(new JLabel("Código/Nombre:"));
        txtBusqueda.setBackground(Color.WHITE);
        top.add(txtBusqueda);
        top.add(btnBuscar);
        btnBuscar.setBackground(Color.GRAY);
        top.add(cbBuscador);
        top.add(new JLabel("Cant.:"));
        spnCantidad.setForeground(new Color(0, 128, 0));
        spnCantidad.setBackground(Color.WHITE);
        top.add(spnCantidad);
        top.add(btnAgregar);
        btnAgregar.setBackground(new Color(50, 205, 50));
        add(top, BorderLayout.NORTH);

        // Panel izquierdo con categorías
        panelCategorias.setBackground(new Color(250, 240, 230));
        panelCategorias.setLayout(new BoxLayout(panelCategorias, BoxLayout.Y_AXIS));
        JScrollPane scrollCategorias = new JScrollPane(panelCategorias);
        scrollCategorias.setPreferredSize(new Dimension(110, 0));

        // Panel de miniaturas con WrapLayout y scroll vertical
        panelMiniaturas.setBackground(new Color(250, 235, 215));
        panelMiniaturas.setLayout(new WrapLayout(FlowLayout.LEFT, 5, 5));
        JScrollPane scrollMiniaturas = new JScrollPane(panelMiniaturas,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollMiniaturas.setPreferredSize(new Dimension(390, 0));

        // Dividir categorías y miniaturas
        JSplitPane splitMini = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollCategorias, scrollMiniaturas);
        splitMini.setDividerLocation(110);

        // Dividir miniaturas y tabla de venta
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, splitMini, crearPanelVentas());
        split.setDividerLocation(500);
        add(split, BorderLayout.CENTER);

        // Panel inferior: totales + pago + cambio + finalizar
        JPanel abajo = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        abajo.setBackground(UIManager.getColor("TabbedPane.background"));

        abajo.add(lblSubtotal);
        abajo.add(lblIva);
        abajo.add(lblTotal);

        // pago y cambio
        abajo.add(lblPago);
        abajo.add(txtPago);
        txtPago.setHorizontalAlignment(JTextField.RIGHT);
        abajo.add(lblCambio);

        btnFinalizar.setBackground(Color.GREEN);
        btnFinalizar.setForeground(Color.BLACK);
        abajo.add(btnFinalizar);

        add(abajo, BorderLayout.SOUTH);
    }

    private JPanel crearPanelVentas() {
        JPanel centro = new JPanel(new BorderLayout(5, 5));
        centro.setBackground(new Color(245, 245, 220));
        // Botones de cantidad y eliminar
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controles.add(btnMas);
        btnMas.setBackground(Color.GREEN);
        controles.add(btnMenos);
        btnMenos.setBackground(Color.RED);
        controles.add(btnEliminar);
        btnEliminar.setBackground(new Color(204, 0, 0));
        controles.setBackground(new Color(250, 235, 215));
        centro.add(controles, BorderLayout.NORTH);
        tblVenta.setBackground(new Color(250, 235, 215));
        // Tabla de venta en scroll
        centro.add(new JScrollPane(tblVenta), BorderLayout.CENTER);

        return centro;
    }

    public void mostrarProductos(List<Producto> productos) {
        panelMiniaturas.removeAll();
        for (Producto p : productos) {
            ImageIcon icon = new ImageIcon(p.getRutaImagen());
            Image img = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            JButton btn = new JButton(new ImageIcon(img));
            btn.setToolTipText(p.getNombre());
            btn.setPreferredSize(new Dimension(90, 90));
            btn.putClientProperty("producto", p);
            panelMiniaturas.add(btn);
        }
        panelMiniaturas.revalidate();
        panelMiniaturas.repaint();
    }

    public void mostrarCategorias(List<String> categorias) {
        panelCategorias.removeAll();

        // —————— Botón “Todos” con listener ——————
        JButton btnTodos = new JButton("Todos");
        btnTodos.setBackground(new Color(173, 216, 230));
        btnTodos.setMaximumSize(new Dimension(100, 40));
        btnTodos.setPreferredSize(new Dimension(100, 40));
        btnTodos.setFont(new Font("Arial", Font.PLAIN, 11));
        btnTodos.setMargin(new Insets(2, 2, 2, 2));
        btnTodos.setFocusPainted(false);
        btnTodos.addActionListener(e -> {
            if (listenerCategoria != null) {
                // pasamos "Todos" al controlador para que cargue todo
                listenerCategoria.filtrar("Todos");
            }
        });
        panelCategorias.add(btnTodos);

        // —————— Botones de cada categoría ——————
        for (String cat : categorias) {
            JButton btnCat = new JButton(cat);
            btnCat.setBackground(new Color(173, 216, 230));
            btnCat.setMaximumSize(new Dimension(100, 40));
            btnCat.setPreferredSize(new Dimension(100, 40));
            btnCat.setFont(new Font("Arial", Font.PLAIN, 11));
            btnCat.setMargin(new Insets(2, 2, 2, 2));
            btnCat.setFocusPainted(false);
            btnCat.addActionListener(e -> {
                if (listenerCategoria != null) {
                    listenerCategoria.filtrar(cat);
                }
            });
            panelCategorias.add(btnCat);
        }

        panelCategorias.revalidate();
        panelCategorias.repaint();
    }

    public JPanel getPanelCategorias() {
        return panelCategorias;
    }

    public JPanel getPanelMiniaturas() {
        return panelMiniaturas;
    }

    public JTable getTblVenta() {
        return tblVenta;
    }
}
