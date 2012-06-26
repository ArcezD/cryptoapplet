package es.uji.apps.cryptoapplet.crypto.facturae;

import java.util.List;

import es.uji.apps.cryptoapplet.config.CryptoAppletException;
import es.uji.apps.cryptoapplet.crypto.DetailsGenerator;
import es.uji.apps.cryptoapplet.crypto.SignatureDetails;

public class FacturaeDetails implements DetailsGenerator
{
    public List<SignatureDetails> getDetails(byte[] data) throws CryptoAppletException
    {
        return SignatureDetails.getSignatureDetailInformation(data,
                "http://uri.etsi.org/01903/v1.3.2#");
    }
}