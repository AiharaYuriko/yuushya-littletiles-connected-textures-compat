import com.yuushya.compat.connected.BoundaryCoverage;
import com.yuushya.compat.connected.BoundaryCoverage.Rect;
import java.util.*;
import java.lang.management.ManagementFactory;

/** Standalone synthetic measurement; excludes world scans, Mixin and rendering. */
public class CoverageBenchmark {
    static volatile boolean sink;
    static final com.sun.management.ThreadMXBean MEM =
        (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
    static final long THREAD = Thread.currentThread().threadId();
    static List<Rect> grid(int n) {
        var r = new ArrayList<Rect>();
        for (int x=0;x<n;x++) for(int y=0;y<n;y++)
            r.add(new Rect(x/(double)n,y/(double)n,(x+1)/(double)n,(y+1)/(double)n));
        return r;
    }
    static List<Rect> strips(int n) {
        var r = new ArrayList<Rect>();
        for(int x=0;x<n;x++) r.add(new Rect(x/(double)n,0,(x+1)/(double)n,1));
        return r;
    }
    static double[] run(List<Rect> input, long duration) {
        long count=0, alloc=MEM.getThreadAllocatedBytes(THREAD), start=System.nanoTime(), elapsed;
        do {
            for(int i=0;i<64;i++) sink=BoundaryCoverage.full(input);
            count+=64; elapsed=System.nanoTime()-start;
        } while(elapsed<duration);
        return new double[]{elapsed/(double)count/1000,
            (MEM.getThreadAllocatedBytes(THREAD)-alloc)/(double)count};
    }
    static void scenario(String name,List<Rect> input) {
        run(input,500_000_000L);
        double[] times=new double[5], bytes=new double[5];
        for(int k=0;k<5;k++){var v=run(input,100_000_000L);times[k]=v[0];bytes[k]=v[1];}
        Arrays.sort(times);Arrays.sort(bytes);
        System.out.printf(Locale.ROOT,"%s,%d,%.3f,%.3f,%.3f,%.0f%n",name,input.size(),times[2],times[0],times[4],bytes[2]);
    }
    public static void main(String[] args) {
        MEM.setThreadAllocatedMemoryEnabled(true);
        System.out.println("case,rectangles,median_us_per_call,min_batch_us,max_batch_us,allocated_bytes_per_call");
        scenario("single_full",grid(1));
        scenario("grid_4x4",grid(4));
        scenario("grid_8x8",grid(8));
        scenario("grid_16x16",grid(16));
        scenario("strips_64",strips(64));
        scenario("strips_256",strips(256));
        scenario("frame_with_hole",List.of(new Rect(0,0,1,.25),new Rect(0,.75,1,1),
            new Rect(0,.25,.25,.75),new Rect(.75,.25,1,.75)));
    }
}
