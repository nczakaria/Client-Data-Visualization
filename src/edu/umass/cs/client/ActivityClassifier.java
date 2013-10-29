package edu.umass.cs.client;

public class ActivityClassifier {

	  public static double classify(Object[] i)
	    throws Exception {

	    double p = Double.NaN;
	    p = ActivityClassifier.N3c1e5dc511(i);
	    return p;
	  }
	  static double N3c1e5dc511(Object []i) {
	    double p = Double.NaN;
	    if (i[19] == null) {
	      p = 1;
	    } else if (((Double) i[19]).doubleValue() <= 0.9355438399175162) {
	    p = ActivityClassifier.N6d9e4f5812(i);
	    } else if (((Double) i[19]).doubleValue() > 0.9355438399175162) {
	    p = ActivityClassifier.N533c697615(i);
	    } 
	    return p;
	  }
	  static double N6d9e4f5812(Object []i) {
	    double p = Double.NaN;
	    if (i[27] == null) {
	      p = 1;
	    } else if (((Double) i[27]).doubleValue() <= 0.814693979504828) {
	      p = 1;
	    } else if (((Double) i[27]).doubleValue() > 0.814693979504828) {
	    p = ActivityClassifier.N346b52a113(i);
	    } 
	    return p;
	  }
	  static double N346b52a113(Object []i) {
	    double p = Double.NaN;
	    if (i[14] == null) {
	      p = 0;
	    } else if (((Double) i[14]).doubleValue() <= 1.6260701635333454) {
	      p = 0;
	    } else if (((Double) i[14]).doubleValue() > 1.6260701635333454) {
	    p = ActivityClassifier.N71fde2e914(i);
	    } 
	    return p;
	  }
	  static double N71fde2e914(Object []i) {
	    double p = Double.NaN;
	    if (i[7] == null) {
	      p = 2;
	    } else if (((Double) i[7]).doubleValue() <= -12828.504535179389) {
	      p = 2;
	    } else if (((Double) i[7]).doubleValue() > -12828.504535179389) {
	      p = 1;
	    } 
	    return p;
	  }
	  static double N533c697615(Object []i) {
	    double p = Double.NaN;
	    if (i[28] == null) {
	      p = 0;
	    } else if (((Double) i[28]).doubleValue() <= 5.138938079086794) {
	    p = ActivityClassifier.N232fecab16(i);
	    } else if (((Double) i[28]).doubleValue() > 5.138938079086794) {
	      p = 2;
	    } 
	    return p;
	  }
	  static double N232fecab16(Object []i) {
	    double p = Double.NaN;
	    if (i[33] == null) {
	      p = 0;
	    } else if (((Double) i[33]).doubleValue() <= 10.281981639941373) {
	    p = ActivityClassifier.N1f5ae09917(i);
	    } else if (((Double) i[33]).doubleValue() > 10.281981639941373) {
	    p = ActivityClassifier.N27ff546820(i);
	    } 
	    return p;
	  }
	  static double N1f5ae09917(Object []i) {
	    double p = Double.NaN;
	    if (i[0] == null) {
	      p = 2;
	    } else if (((Double) i[0]).doubleValue() <= -0.5994187915484543) {
	    p = ActivityClassifier.N74f6ce418(i);
	    } else if (((Double) i[0]).doubleValue() > -0.5994187915484543) {
	    p = ActivityClassifier.N6f75f07b19(i);
	    } 
	    return p;
	  }
	  static double N74f6ce418(Object []i) {
	    double p = Double.NaN;
	    if (i[10] == null) {
	      p = 1;
	    } else if (((Double) i[10]).doubleValue() <= 1.052998558968653) {
	      p = 1;
	    } else if (((Double) i[10]).doubleValue() > 1.052998558968653) {
	      p = 2;
	    } 
	    return p;
	  }
	  static double N6f75f07b19(Object []i) {
	    double p = Double.NaN;
	    if (i[10] == null) {
	      p = 0;
	    } else if (((Double) i[10]).doubleValue() <= 1.7652469607398276) {
	      p = 0;
	    } else if (((Double) i[10]).doubleValue() > 1.7652469607398276) {
	      p = 1;
	    } 
	    return p;
	  }
	  static double N27ff546820(Object []i) {
	    double p = Double.NaN;
	    if (i[28] == null) {
	      p = 0;
	    } else if (((Double) i[28]).doubleValue() <= 2.2963355310801936) {
	      p = 0;
	    } else if (((Double) i[28]).doubleValue() > 2.2963355310801936) {
	    p = ActivityClassifier.N6bfe8c5b21(i);
	    } 
	    return p;
	  }
	  static double N6bfe8c5b21(Object []i) {
	    double p = Double.NaN;
	    if (i[8] == null) {
	      p = 0;
	    } else if (((Double) i[8]).doubleValue() <= 1.058995587477991E7) {
	      p = 0;
	    } else if (((Double) i[8]).doubleValue() > 1.058995587477991E7) {
	      p = 2;
	    } 
	    return p;
	  }
	}
