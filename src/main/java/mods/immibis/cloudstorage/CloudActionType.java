package mods.immibis.cloudstorage;

public enum CloudActionType {
	EXTRACT(CloudActionExtract.class),
	STOCK(CloudActionStock.class),
	INSERT(CloudActionInsert.class),
	EXTRACT_RF(CloudActionExtractRF.class),
	INSERT_RF(CloudActionInsertRF.class),
	EXTRACT_EU(CloudActionExtractEU.class),
	INSERT_EU(CloudActionInsertEU.class),
	//INSERT_MJ(CloudActionInsertMJ.class),
	EXTRACT_FLUID(CloudActionExtractFluid.class),
	INSERT_FLUID(CloudActionInsertFluid.class),
	;
	
	private CloudActionType(Class<? extends CloudAction> clazz) {
		this.clazz = clazz;
	}
	
	public final Class<? extends CloudAction> clazz;
}
