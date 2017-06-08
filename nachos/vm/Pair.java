package nachos.vm;

public class Pair {
    public Pair(int pid, int vpn) {
        this.pid = pid;
        this.vpn = vpn;
    }

    public int hashCode() {
        return (pid.toString() + "|" + vpn.toString()).hashCode();
    }

    public boolean equals(Object o) {
        if (o == null)
            return false;
        else if (o instanceof Pair) {
            Pair p1 = (Pair) o;
            return p1.vpn.equals(this.vpn) && p1.pid.equals(this.pid);
        }
    }

    Integer pid;
    Integer vpn;
}
