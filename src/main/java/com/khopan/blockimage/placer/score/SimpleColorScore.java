package com.khopan.blockimage.placer.score;

class SimpleColorScore implements ColorScore {
	@Override
	public double score(int alphaX, int redX, int greenX, int blueX, int alphaY, int redY, int greenY, int blueY) {
		double factor = ((double) Math.min(alphaX, alphaY)) / 255.0d;
		return (1.0d - factor) * Math.abs(alphaX - alphaY) * 3.0d + factor * (Math.abs(redX - redY) + Math.abs(greenX - greenY) + Math.abs(blueX - blueY));
	}
}
