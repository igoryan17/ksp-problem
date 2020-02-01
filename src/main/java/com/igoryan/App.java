package com.igoryan;

import static com.igoryan.util.TopologyUtil.simulateSdWanTopology;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.google.common.base.Stopwatch;
import com.google.common.graph.MutableNetwork;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.igoryan.apsp.AllPairsCalculator;
import com.igoryan.model.Algorithms;
import com.igoryan.model.network.Node;
import com.igoryan.model.network.ParallelEdges;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "com.igoryan.benchmark")
public class App {

  private static final String ALGORITHM_PROPERTY = "algorithm";
  private static final String TOPOLOGY_WITH_NO_TRANSIT_PROPERTY = "topology.no_transit";
  private static final String GW_COUNT_PROPERTY = "topology.gw.count";
  private static final String LINK_BETWEEN_GW_ENABLED_PROPERTY = "topology.gw.link.enabled";
  private static final String CPE_MAX_COUNT_PROPERTY = "topology.cpe.count.max";
  private static final String CPE_START_COUNT_PROPERTY = "topology.cpe.count.start";
  private static final String CPE_GROWING_STEP_PROPERTY = "topology.cpe.discovering.step";
  private static final String PATHS_PER_PAIR_COUNT = "topology.paths.per_pair.count";
  private static final String REPEATS_COUNT_PROPERTY = "benchmarks.repeated.count";
  private static final String TIMEOUT_AFTER_EACH_CALCULATION = "benchmarks.calc.timeout.ms";
  private static final String TIMEOUT_AFTER_REPEATS_PROPERTY = "benchmarks.series.timeout.ms";
  private static final String PATH_TO_REPORT_PROPERTY = "benchmarks.report.path";

  public static void main(String[] args) throws IOException, InterruptedException {
    final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    final Properties appProperties = new Properties();
    appProperties.load(classloader.getResourceAsStream("application.properties"));

    final Algorithms algorithm =
        Algorithms.valueOf(getSystemPropertyOrDefaultOfApp(appProperties, ALGORITHM_PROPERTY));
    final boolean withNoTransit =
        Boolean.parseBoolean(
            getSystemPropertyOrDefaultOfApp(appProperties, TOPOLOGY_WITH_NO_TRANSIT_PROPERTY));
    final List<Module> modules = new ArrayList<>();
    switch (algorithm) {
      case YEN:
        if (withNoTransit) {
          modules.add(new DijkstraWithNoTransitModule());
          modules.add(new YenWithNoTransitModule());
        } else {
          modules.add(new DijkstraModule());
          modules.add(new YenModule());
        }
        break;
      case MPS:
        if (withNoTransit) {
          modules.add(new DijkstraWithNoTransitModule());
          modules.add(new MpsWithNoTransitModule());
        } else {
          modules.add(new DijkstraModule());
          modules.add(new MpsModule());
        }
        break;
      case NAIVE:
        if (withNoTransit) {
          modules.add(new DijkstraModule());
          modules.add(new YenNaiveModule());
        } else {
          throw new IllegalArgumentException("naive algorithm applicable only for no transit case");
        }
        break;
      default:
        throw new IllegalArgumentException("unknown algorithm: " + algorithm);
    }
    final Injector injector = Guice.createInjector(modules);
    final AllPairsCalculator allPairsCalculator = injector.getInstance(AllPairsCalculator.class);
    final int gwCount =
        Integer.parseInt(getSystemPropertyOrDefaultOfApp(appProperties, GW_COUNT_PROPERTY));
    final boolean linksBetweenGwEnabled = Boolean.parseBoolean(
        getSystemPropertyOrDefaultOfApp(appProperties, LINK_BETWEEN_GW_ENABLED_PROPERTY));
    final int cpeMaxCount =
        Integer.parseInt(getSystemPropertyOrDefaultOfApp(appProperties, CPE_MAX_COUNT_PROPERTY));
    final int cpeGrowingStep =
        Integer.parseInt(getSystemPropertyOrDefaultOfApp(appProperties, CPE_GROWING_STEP_PROPERTY));
    final int pathsPerPair =
        Integer.parseInt(getSystemPropertyOrDefaultOfApp(appProperties, PATHS_PER_PAIR_COUNT));
    final int repeatsCount =
        Integer.parseInt(getSystemPropertyOrDefaultOfApp(appProperties, REPEATS_COUNT_PROPERTY));
    final int timeoutBetweenEachCalculation = Integer
        .parseInt(getSystemPropertyOrDefaultOfApp(appProperties, TIMEOUT_AFTER_EACH_CALCULATION));
    final int timeoutMs = Integer
        .parseInt(getSystemPropertyOrDefaultOfApp(appProperties, TIMEOUT_AFTER_REPEATS_PROPERTY));
    int currentCpeCount =
        Integer.parseInt(getSystemPropertyOrDefaultOfApp(appProperties, CPE_START_COUNT_PROPERTY));
    final Path reportPath =
        Paths.get(getSystemPropertyOrDefaultOfApp(appProperties, PATH_TO_REPORT_PROPERTY));
    if (Files.notExists(reportPath)) {
      Files.createFile(reportPath);
    }
    while (currentCpeCount < cpeMaxCount) {
      for (int i = 0; i < repeatsCount; i++) {
        log.info("perform benchmark; step number: {}, cpeCount: {}", i, currentCpeCount);
        final MutableNetwork<Node, ParallelEdges> network =
            simulateSdWanTopology(gwCount, currentCpeCount, linksBetweenGwEnabled);
        final Stopwatch stopwatch = Stopwatch.createStarted();
        allPairsCalculator.calculate(network, pathsPerPair);
        stopwatch.stop();
        Files.write(reportPath,
            ("calc_time," + stopwatch.elapsed(MILLISECONDS) + ",cpe_count," + currentCpeCount
                + "\n")
                .getBytes(), APPEND);
        Thread.sleep(timeoutBetweenEachCalculation);
      }
      currentCpeCount += cpeGrowingStep;
      Thread.sleep(timeoutMs);
    }
  }

  private static String getSystemPropertyOrDefaultOfApp(final @NonNull Properties appProperties,
      final @NonNull String propertyName) {
    return System.getProperty(propertyName, appProperties.getProperty(propertyName));
  }
}
