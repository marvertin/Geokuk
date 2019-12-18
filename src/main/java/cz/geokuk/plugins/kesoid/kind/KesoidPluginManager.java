package cz.geokuk.plugins.kesoid.kind;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import cz.geokuk.plugins.kesoid.importek.GpxWpt;
import cz.geokuk.plugins.kesoid.kind.cgp.CgpPlugin;
import cz.geokuk.plugins.kesoid.kind.kes.KesPlugin;
import cz.geokuk.plugins.kesoid.kind.munzee.MunzeePlugin;
import cz.geokuk.plugins.kesoid.kind.photo.PhotoPlugin;
import cz.geokuk.plugins.kesoid.kind.simplewaypoint.SimpleWaypointGpxWptProcak;
import cz.geokuk.plugins.kesoid.kind.waymark.WaymarkPlugin;
import cz.geokuk.util.procak.ProcakDispatcher;
import lombok.Getter;

/**
 * Manager kesouidových pluginů.
 * Je to vstupní bod pro práci s pluginy.
 * KesoidPluginManager se injektuje těm, kteří potřebujídělat něco, co je specifické pro jednotlivé druhy kešoidů.
 * Je snaha specifikum kešoidů soustředit v jednom balíku.
 * @author Martin
 *
 */
public class KesoidPluginManager {

	@Getter
	private  final CgpPlugin cgpPlugin = new CgpPlugin();

	@Getter
	private final KesPlugin kesPlugin = new KesPlugin();

	@Getter
	private  final WaymarkPlugin waymarkPlugin = new WaymarkPlugin();

	@Getter
	private  final MunzeePlugin munzeePlugin = new MunzeePlugin();

	@Getter
	private  final PhotoPlugin photoPlugin = new PhotoPlugin();

	@Getter
	private final List<KesoidPlugin> plugins
	= Lists.newArrayList(
			cgpPlugin,
			kesPlugin,
			waymarkPlugin,
			munzeePlugin,
			photoPlugin
			);


	/**
	 * Zřídíme procák dispatchera, který bude jednotlivými poskytovateli publikovat waypointy do buldera.
	 * @param builder
	 * @return
	 */
	public ProcakDispatcher<GpxWpt> createGpxWptProcakDispatcher(final GpxToWptContext ctx, final GpxToWptBuilder builder) {
		return new ProcakDispatcher<>(plugins.stream()
				.map(plugin -> plugin.createGpxWptProcak(ctx,
						wpt -> {
							wpt.setKesoidPlugin(plugin);
							builder.expose(wpt);
						}))
				.filter(Objects::nonNull)
				.collect(Collectors.toList()),

				new SimpleWaypointGpxWptProcak(ctx, builder)
				);

	}

}
