import com.yuushya.compat.connected.BoundaryCoverage;
import com.yuushya.compat.connected.BoundaryCoverage.Rect;
import java.util.*;

/** Independent 8x8-cell oracle; generated rectangle boundaries lie on that grid. */
public class CoverageOracle {
    static boolean oracle(List<Rect> rs) {
        for(int x=0;x<8;x++) for(int y=0;y<8;y++) {
            double u=(x+.5)/8, v=(y+.5)/8;
            boolean hit=false;
            for(Rect r:rs) if(r.minU()<=u && r.maxU()>=u && r.minV()<=v && r.maxV()>=v) {hit=true;break;}
            if(!hit)return false;
        }
        return true;
    }
    public static void main(String[] args) {
        Random rng=new Random(216217L);
        int checks=0;
        for(int trial=0;trial<20000;trial++) {
            var rs=new ArrayList<Rect>();
            if(trial%2==0) {
                for(int x=0;x<8;x++) for(int y=0;y<8;y++) rs.add(new Rect(x/8.,y/8.,(x+1)/8.,(y+1)/8.));
                if(trial%4==0)rs.remove(rng.nextInt(rs.size()));
            } else {
                int n=rng.nextInt(40);
                for(int i=0;i<n;i++) {
                    double a=(rng.nextInt(13)-2)/8., b=(rng.nextInt(13)-2)/8.;
                    double c=(rng.nextInt(13)-2)/8., d=(rng.nextInt(13)-2)/8.;
                    rs.add(new Rect(Math.min(a,b),Math.min(c,d),Math.max(a,b),Math.max(c,d)));
                }
            }
            boolean expected=oracle(rs);
            Collections.shuffle(rs,rng);
            if(BoundaryCoverage.full(rs)!=expected)throw new AssertionError("coverage trial "+trial);
            var transposed=rs.stream().map(r->new Rect(r.minV(),r.minU(),r.maxV(),r.maxU())).toList();
            if(BoundaryCoverage.full(transposed)!=expected)throw new AssertionError("transpose trial "+trial);
            checks+=2;
        }
        System.out.println("PASS: "+checks+" independent raster-oracle and axis-symmetry checks");
    }
}
