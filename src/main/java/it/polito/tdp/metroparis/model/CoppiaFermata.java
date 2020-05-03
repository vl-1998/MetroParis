package it.polito.tdp.metroparis.model;

public class CoppiaFermata {
	private Fermata fp;
	private Fermata fa;
	/**
	 * @param fp
	 * @param fa
	 */
	public CoppiaFermata(Fermata fp, Fermata fa) {
		super();
		this.fp = fp;
		this.fa = fa;
	}
	public Fermata getFp() {
		return fp;
	}
	public void setFp(Fermata fp) {
		this.fp = fp;
	}
	public Fermata getFa() {
		return fa;
	}
	public void setFa(Fermata fa) {
		this.fa = fa;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fa == null) ? 0 : fa.hashCode());
		result = prime * result + ((fp == null) ? 0 : fp.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CoppiaFermata other = (CoppiaFermata) obj;
		if (fa == null) {
			if (other.fa != null)
				return false;
		} else if (!fa.equals(other.fa))
			return false;
		if (fp == null) {
			if (other.fp != null)
				return false;
		} else if (!fp.equals(other.fp))
			return false;
		return true;
	}
	
	
	
}
