package in.blacklotus;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

public class YahooFinanceApp {

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					YahooFinanceApp window = new YahooFinanceApp();
					window.show(new String[] { "#", "details" },
							new String[][] { { "1", "[$23.34] of [GT] is around target price: $24.34 and target percent: 0.5%" }, 
						{ "1", "[$23.34] of [GT] is around target price: $24.34 and target percent: 0.5%" } });
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public YahooFinanceApp() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(300, 200, 800, 480);
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
	}

	/**
	 * Show the frame
	 */
	public void show(String[] headers, String[][] data) {
		JTable jt = new JTable(data, headers);
		jt.setBounds(0, 0, 800, 480);
		jt.setShowHorizontalLines(true);
		jt.getShowVerticalLines();
		jt.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		jt.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;

			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Border border = BorderFactory.createEtchedBorder();
				JComponent comp = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
						row, column);
				comp.setBorder(border);
				return comp;
			}
		});
		jt.setRowHeight(30);

		TableColumnModel columnModel = jt.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(30);
		columnModel.getColumn(1).setPreferredWidth(800);
		JScrollPane sp = new JScrollPane(jt);
		frame.add(sp, BorderLayout.CENTER);
		frame.validate();
		frame.repaint();
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * Hide the frame
	 */
	public void hide() {
		frame.removeAll();
		frame.setVisible(false);
	}
}
