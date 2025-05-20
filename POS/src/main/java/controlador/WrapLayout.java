package controlador;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;


public class WrapLayout extends FlowLayout {
    private static final long serialVersionUID = 1L;


    public WrapLayout() {
        super();
    }

    public WrapLayout(int align) {
        super(align);
    }


    public WrapLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        return calcularTamanho(target, true);
    }

    @Override
    public Dimension minimumLayoutSize(Container target) {
        Dimension minimo = calcularTamanho(target, false);
        // Restar el espacio horizontal para asegurar que quepa
        minimo.width -= (getHgap() + 1);
        return minimo;
    }

    private Dimension calcularTamanho(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            int anchoContenedor = target.getSize().width;
            if (anchoContenedor == 0) {
                anchoContenedor = Integer.MAX_VALUE;
            }

            Insets insets = target.getInsets();
            int hgap = getHgap();
            int vgap = getVgap();
            int maxWidth = anchoContenedor - (insets.left + insets.right + hgap * 2);

            Dimension dimension = new Dimension(0, 0);
            int anchoFila = 0;
            int altoFila = 0;

            for (Component comp : target.getComponents()) {
                if (!comp.isVisible()) continue;
                Dimension d = preferred ? comp.getPreferredSize() : comp.getMinimumSize();
                if (anchoFila + d.width > maxWidth) {
                    // Nueva fila
                    dimension.width = Math.max(dimension.width, anchoFila);
                    dimension.height += altoFila + vgap;
                    anchoFila = 0;
                    altoFila = 0;
                }
                anchoFila += d.width + hgap;
                altoFila = Math.max(altoFila, d.height);
            }
            // Fila final
            dimension.width = Math.max(dimension.width, anchoFila);
            dimension.height += altoFila;

            // Incluir márgenes
            dimension.width += insets.left + insets.right + hgap * 2;
            dimension.height += insets.top + insets.bottom + vgap * 2;

            // Ajuste si está dentro de un JScrollPane
            Container scrollPane = SwingUtilities.getAncestorOfClass(JScrollPane.class, target);
            if (scrollPane != null) {
                dimension.width -= (hgap + 1);
            }

            return dimension;
        }
    }
}
