package ch.epfl.isochrone.timetable;

import java.util.Collections;

import org.junit.Ignore;
import org.junit.Test;

import ch.epfl.isochrone.timetable.Date.DayOfWeek;
import ch.epfl.isochrone.timetable.Date.Month;

import static org.junit.Assert.assertEquals;

public class TestService {
    
    @Test
    public void testIsOperatingOn() {
        Date start = new Date(22, Month.MARCH, 2014);
        Date end = new Date(30, Month.MARCH, 2014);

        Service.Builder sb = new Service.Builder("s", start, end);
        sb.addOperatingDay(DayOfWeek.FRIDAY);
        Service ss = sb.build();
        
        Date testDate1 = new Date(27, Month.MARCH, 2014);
     //   sb.addIncludedDate(testDate);
        Date testDate2 = new Date(28, Month.MARCH, 2014);
        Date testDate3 = new Date(21, Month.MARCH, 2014);
        Date testDate4 = new Date(26, Month.MARCH, 2014);

        assertEquals(false,ss.isOperatingOn(testDate1));
        assertEquals(true,ss.isOperatingOn(testDate2));
        assertEquals(false,ss.isOperatingOn(testDate3));
        assertEquals(false,ss.isOperatingOn(testDate4));
        
        sb.addOperatingDay(DayOfWeek.THURSDAY);
        Service ss1 = sb.build();
        assertEquals(true,ss1.isOperatingOn(testDate1));
        
       
        sb.addIncludedDate(testDate4);
        Service ss2 = sb.build();
        assertEquals(true,ss2.isOperatingOn(testDate4));
        
        sb.addExcludedDate(testDate2);
        Service ss3 = sb.build();
        assertEquals(false,ss3.isOperatingOn(testDate2));
       
    }
    
    @Test
    public void testImmutability() {
        Date start = new Date(22, Month.MARCH, 2014);
        Date end = new Date(30, Month.MARCH, 2014);
        
        Service.Builder sb = new Service.Builder("s", start, end);
        sb.addOperatingDay(DayOfWeek.FRIDAY);
        Service ss = sb.build();
        
        Date testDate1 = new Date(27, Month.MARCH, 2014);
     //   sb.addIncludedDate(testDate);
        Date testDate2 = new Date(28, Month.MARCH, 2014);
        Date testDate3 = new Date(21, Month.MARCH, 2014);
        Date testDate4 = new Date(26, Month.MARCH, 2014);
        
        assertEquals(false,ss.isOperatingOn(testDate1));
        assertEquals(true,ss.isOperatingOn(testDate2));
        assertEquals(false,ss.isOperatingOn(testDate3));
        assertEquals(false,ss.isOperatingOn(testDate4));
        
        sb.addIncludedDate(testDate1);
        sb.addExcludedDate(testDate2);
        
        assertEquals(false,ss.isOperatingOn(testDate1));
        assertEquals(true,ss.isOperatingOn(testDate2));
     
    }

    // Le "test" suivant n'en est pas un à proprement parler, raison pour
    // laquelle il est ignoré (annotation @Ignore). Son seul but est de garantir
    // que les noms des classes et méthodes sont corrects.
    @Test
    @Ignore
    public void namesAreOk() {
        Date d = new Date(1, Month.JANUARY, 2000);
        Service s = new Service("s",
                d, d,
                Collections.<Date.DayOfWeek> emptySet(),
                Collections.<Date> emptySet(),
                Collections.<Date> emptySet());
        s.name();
        s.isOperatingOn(d);

        Service.Builder sb = new Service.Builder("s", d, d);
        sb.name();
        sb.addOperatingDay(DayOfWeek.MONDAY);
        sb.addExcludedDate(d);
        sb.addIncludedDate(d);
        sb.build();
    }

    // A compléter avec de véritables méthodes de test...
}
