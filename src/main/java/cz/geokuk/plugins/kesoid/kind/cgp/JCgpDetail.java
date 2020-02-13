package cz.geokuk.plugins.kesoid.kind.cgp;

import javax.swing.Box;
import javax.swing.JLabel;

import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.detail.JKesoidDetail0;

/**
 * Detailní informace o vybrané keši.
 *
 * @author Spikodrob
 *
 */
public class JCgpDetail extends JKesoidDetail0 {

	/**
	 *
	 */
	private static final long serialVersionUID = -3323887260932949747L;

	private CzechGeodeticPoint cgp;

	private JLabel jXjtsk;
	private JLabel jYjtsk;

	public JCgpDetail() {
		initComponents();
	}

	@Override
	public void napln(final Wpt wpt) {
		cgp = (CzechGeodeticPoint) wpt.getKoid();
		jXjtsk.setText(cgp.getXjtsk() + "");
		jYjtsk.setText(cgp.getYjtsk() + "");
	}

	private void initComponents() {
		jXjtsk = new JLabel();
		jYjtsk = new JLabel();

		final Box hlav = Box.createVerticalBox();
		add(hlav);

		final Box box2 = Box.createHorizontalBox();
		box2.add(new JLabel("y = "));
		box2.add(jYjtsk);
		box2.add(Box.createHorizontalStrut(20));
		box2.add(new JLabel("x = "));
		box2.add(jXjtsk);
		hlav.add(box2);

		//
		//
		// Box pan4b = Box.createVerticalBox();
		//
		// box2.add(Box.createHorizontalGlue());
		//
		// // pan4.setAlignmentX(RIGHT_ALIGNMENT);
		// box2.add(pan4b);
		//
		//
		// hlav.add(box2);
		//
		// Box box3 = Box.createHorizontalBox();
		// box3.add(Box.createGlue());
		// hlav.add(box3);
	}

}
