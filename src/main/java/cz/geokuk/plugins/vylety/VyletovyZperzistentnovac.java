package cz.geokuk.plugins.vylety;

import java.io.*;
import java.util.*;

import cz.geokuk.core.program.FConst;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VyletovyZperzistentnovac {



	private KesoidModel kesoidModel;

	public Vylet immediatlyNactiVylet(final KesBag vsechny) {
		try {
			final Vylet novyvylet = new Vylet();
			aktualizujVylet(novyvylet, loadGgt(kesoidModel.getUmisteniSouboru().getAnoGgtFile().getEffectiveFile()), EVylet.ANO, vsechny);
			aktualizujVylet(novyvylet, loadGgt(kesoidModel.getUmisteniSouboru().getNeGgtFile().getEffectiveFile()), EVylet.NE, vsechny);
			return novyvylet;
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void immediatlyZapisVylet(final Vylet vylet) {
		zapis(vylet, kesoidModel.getUmisteniSouboru().getAnoGgtFile().getEffectiveFile(), EVylet.ANO);
		zapis(vylet, kesoidModel.getUmisteniSouboru().getNeGgtFile().getEffectiveFile(), EVylet.NE);
	}

	public void inject(final KesoidModel kesoidModel) {
		this.kesoidModel = kesoidModel;
	}

	private void aktualizujVylet(final Vylet novyvylet, final VyletPul vyletPul, final EVylet evyl, final KesBag vsechny) {
		if (vsechny != null) {
			for (final Wpt wpt : vsechny.getWpts()) {
				if (vyletPul.kesides.contains(wpt.getIdentifier())) {
					novyvylet.add(evyl, wpt);
				}
			}
		}
	}

	private void flushToFile(final Collection<String> lines, final File file, final boolean append) {
		try (BufferedWriter wrt = new BufferedWriter(new FileWriter(file, append))) {
			// TODO : portability
			for (final String s : lines) {
				wrt.write(String.format("%s%s", s, FConst.NL));
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private VyletPul loadGgt(final BufferedReader reader) throws IOException {
		String line;
		final Set<String> set = new HashSet<>();
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.isEmpty()) {
				continue;
			}
			set.add(line);
		}
		return new VyletPul(set);
	}

	private VyletPul loadGgt(final File file) throws IOException {
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			return loadGgt(br);
		} catch (final FileNotFoundException e) {
			return new VyletPul(new HashSet<>());
		}
	}

	private void zapis(final Vylet vylet, final File file, final EVylet evyl) {
		final Set<Wpt> wpts = vylet.get(evyl);
		final Set<String> tripGeocodes = new HashSet<>(wpts.size());
		for (final Wpt wpt : wpts) {
			tripGeocodes.add(wpt.getIdentifier());
		}
		final List<String> toWrite = new ArrayList<>();

		boolean rewrite = false;

		if (file.exists() && file.canRead()) {
			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
				String line;
				while ((line = br.readLine()) != null) {
					if (tripGeocodes.contains(line)) {
						toWrite.add(line);
						tripGeocodes.remove(line);
					} else {
						rewrite = true;
					}
				}
			} catch (final IOException e) {
				rewrite = true;
				log.error("Error while reading a trip file!", e);
			}
		}

		if (rewrite) {
			toWrite.addAll(tripGeocodes);
			flushToFile(toWrite, file, false);
			log.info("Rewritten all for trip {}.", evyl);
		} else if (!tripGeocodes.isEmpty()) {
			flushToFile(tripGeocodes, file, true);
			log.info("Appended {} caches(s) for trip {}", tripGeocodes.size(), evyl);
		} else {
			log.info("No changes detected, keeping the original file for trip {}.", evyl);
		}
	}
}
