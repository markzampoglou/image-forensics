package pkg08_JPEGGhost;

import java.awt.image.BufferedImage;
import java.util.concurrent.*;

/**
 * Created by marzampoglou on 11/4/15.
 */
public class GhostCalculator {

    /**
     * This class implements multi-threaded image Ghost extraction.
     *
     * @author Markos Zampoglou, based on the work of Eleftherios Spyromitros-Xioufis
     *
     */

        private ExecutorService ghostExecutor;

        private CompletionService<GhostCalculationResult> pool;

        /** The current number of tasks whose termination is pending. **/
        private int numPendingTasks;

        /**
         * The maximum allowable number of pending tasks, used to limit the memory usage.
         */
        private final int maxNumPendingTasks;


        private int MaxImageSmallDimension;
        private BufferedImage OrigImage;
        private int[][][] OrigByteImage;



        /**
         * Constructor of the multi-threaded download class.
         *
         * @param numThreads
         *            the number of download threads to use
         * @param downloadFolder
         *            the download folder
         */
        public GhostCalculator(int numThreads,int MaxImageSmallDimension, BufferedImage OrigImage, int[][][] OrigByteImage) {
            ghostExecutor = Executors.newFixedThreadPool(numThreads);
            pool = new ExecutorCompletionService<GhostCalculationResult>(ghostExecutor);
            numPendingTasks = 0;
            maxNumPendingTasks = numThreads;
            this.MaxImageSmallDimension=MaxImageSmallDimension;
            this.OrigImage=OrigImage;
            this.OrigByteImage=OrigByteImage;
        }

        public void submitGhostTask(int Quality) {
            Callable<GhostCalculationResult> call = new GhostCalculation(Quality, OrigImage, OrigByteImage, MaxImageSmallDimension);
            pool.submit(call);
            numPendingTasks++;
        }

        /**
         * Gets an image download results from the pool.
         *
         * @return the download result, or null in no results are ready
         * @throws Exception
         *             for a failed download task
         */
        public GhostCalculationResult getGhostCalculationResult() throws Exception {
            Future<GhostCalculationResult> future = pool.poll();
            if (future == null) { // no completed tasks in the pool
                return null;
            } else {
                try {
                    GhostCalculationResult ghor = future.get();
                    return ghor;
                } catch (Exception e) {
                    System.out.println("oops");
                    throw e;
                } finally {
                    // in any case (Exception or not) the numPendingTask should be reduced
                    numPendingTasks--;
                }
            }
        }

        /**
         * Gets an image download result from the pool, waiting if necessary.
         *
         * @return the download result
         * @throws Exception
         *             for a failed download task
         */
        public GhostCalculationResult getGhostCalculationResultWait() throws Exception {
            try {
                GhostCalculationResult ghor = pool.take().get();
                return ghor;
            } catch (Exception e) {
                throw e;
            } finally {
                // in any case (Exception or not) the numPendingTask should be reduced
                numPendingTasks--;
            }
        }

        /**
         * Returns true if the number of pending tasks is smaller than the maximum allowable number.
         *
         * @return
         */
        public boolean canAcceptMoreTasks() {
            if (numPendingTasks < maxNumPendingTasks) {
                return true;
            } else {
                return false;
            }
        }

        public void shutDown() {
            ghostExecutor.shutdown(); // Disable new tasks from being submitted
            try {
                // Wait a while for existing tasks to terminate
                if (!ghostExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                    ghostExecutor.shutdownNow(); // Cancel currently executing tasks
                    // Wait a while for tasks to respond to being cancelled
                    if (!ghostExecutor.awaitTermination(60, TimeUnit.SECONDS))
                        System.err.println("Pool did not terminate");
                }
            } catch (InterruptedException ie) {
                // (Re-)Cancel if current thread also interrupted
                ghostExecutor.shutdownNow();
                // Preserve interrupt status
                Thread.currentThread().interrupt();
            }
        }
    }

