package cz.geokuk.plugins.kesoidobsazenost;

import java.awt.*;
import java.awt.event.MouseEvent;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cz.geokuk.core.coord.JSingleSlide0;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.framework.AfterEventReceiverRegistrationInit;
import cz.geokuk.framework.MouseGestureContext;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.kind.KesoidPluginManager;
import cz.geokuk.plugins.kesoid.mvc.KeskyNactenyEvent;
import cz.geokuk.util.gui.JBarvovyDvojSlider;
import cz.geokuk.util.index2d.BoundingRect;
import cz.geokuk.util.index2d.Indexator;

public class JObsazenost extends JSingleSlide0 implements AfterEventReceiverRegistrationInit {

	// g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
	/**
	 * @author Martin Veverka
	 *
	 */
	private final class ChangeListenerImplementation implements ChangeListener {
		@Override
		public void stateChanged(final ChangeEvent aArg0) {
			final ObsazenostSettings data = obsazenostModel.getData();
			data.setColor(iSlidovnik.getColor());
			obsazenostModel.setData(data);
		}
	}

	private static final long serialVersionUID = -5858146658366237217L;

	private ObsazenostSettings obsazenost;

	private Indexator<Wpt> iIndexator;

	private JBarvovyDvojSlider iSlidovnik;

	private ObsazenostModel obsazenostModel;

	private KesoidPluginManager kesoidPluginManager;

	public JObsazenost() {
		setOpaque(false);
		setCursor(null);
		initComponents();
	}

	@Override
	public JSingleSlide0 createRenderableSlide() {
		return new JObsazenost();
	}

	@Override
	public void initAfterEventReceiverRegistration() {
		registerEvents();
	}

	public void inject(final ObsazenostModel obsazenostModel) {
		this.obsazenostModel = obsazenostModel;
	}

	public void inject(final KesoidPluginManager kesoidPluginManager) {
		this.kesoidPluginManager = kesoidPluginManager;
	}

	@Override
	public void mouseDragged(final MouseEvent e, final MouseGestureContext ctx) {
		// ctx.setMouseCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		super.mouseDragged(e, ctx);
	}

	public void onEvent(final KeskyNactenyEvent event) {
		iIndexator = event.getVsechny().getIndexator();
		repaint();
	}

	public void onEvent(final ObsazenostOnoffEvent event) {
		setVisible(event.isOnoff());
		repaint();
	}

	public void onEvent(final ObsazenostPreferencesChangeEvent event) {
		obsazenost = event.obsazenost;
		iSlidovnik.setColor(obsazenost.getColor());
		repaint();
	}

	@Override
	public void paintComponent(final Graphics aG) {
		if (iIndexator == null) {
			return;
		}
		final Graphics2D g = (Graphics2D) aG;
		final int r =  (int) (getSoord().getPixluNaMetr() * kesoidPluginManager.getMaxPolomerObsazenosti());
		final int d = 2 * r;
		if (d < 4) {
			return; // nemá smysl kreslit malé kroužky
		}
		// obsazenost.setColor(new Color(128,128,128,128));
		g.setColor(obsazenost.getColor());
		final int mouokraj = (int) (getSoord().getMouboduNaMetr() * kesoidPluginManager.getMaxPolomerObsazenosti());
		final BoundingRect boundingRect = getSoord().getBoundingRect().rozsir(mouokraj);
		// final Area area = new Area();
		iIndexator.bound(boundingRect).stream().forEach(wpt -> {
			final int polomerObsazenosti = wpt.getPolomerObsazenosti();
			if (polomerObsazenosti == 0) {
				return;
			}
			final Mou mou = wpt.getMou();
			final Point p = getSoord().transform(mou);
			// Ellipse2D kruh = new Ellipse2D.Float(p.x -r, p.y - r, d, d);
			// Area areakruh = new Area(kruh);
			// area.add(areakruh);
			g.fillOval(p.x - r, p.y - r, d, d);
		});

		// g.fill(area);
	}

	/**
	 *
	 */
	private void initComponents() {
		setLayout(new BorderLayout());
		iSlidovnik = new JBarvovyDvojSlider();
		add(iSlidovnik, BorderLayout.EAST);
		iSlidovnik.getBarvovnik().setToolTipText("Nastavení stupně šedi kruhů (161 m), kterými se zobrazí kešemi obsazené oblasti.");
		iSlidovnik.getPruhlednik().setToolTipText("Nastavení průhlednosti kruhů (161 m), kterými se zobrazí kešemi obsazené oblasti.");
	}

	/**
	 *
	 */
	private void registerEvents() {
		iSlidovnik.addChangeListener(new ChangeListenerImplementation());
	}
}
