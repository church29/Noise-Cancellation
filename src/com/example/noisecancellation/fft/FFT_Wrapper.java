package com.example.noisecancellation.fft;

import edu.emory.mathcs.jtransforms.fft.*;

/**
 * Wrapper class for the JTransforms DFT Library.
 *
 * Support for both double- and single-precision
 * transforms have been implemented. Single
 * precision is faster, but less precise.
 *
 * All functionality from the previous FFT
 * library have also been implemented using
 * the JTransforms library. The tests were
 * essentially the same (with a small
 * exception). Results can be seen in the
 * main function.
 */
public final class FFT_Wrapper
{
    /*-----------------------------------------
     * LITERAL CONSTANTS:
     * 	    INITIAL_SIZE - initial size of
     * 	                   the FFT plans
     *---------------------------------------*/
    private final int INITIAL_SIZE = 1;

    /*-----------------------------------------
     * GLOBAL VARIABLES:
     * 	    n           - size of FFT plans
     * 	    d_fft_1d    - double FFT class
     * 	    f_fft_1d    - float FFT class
     *---------------------------------------*/
    private int             n;
    private DoubleFFT_1D    d_fft_1d;
    private FloatFFT_1D     f_fft_1d;


    /**
     * Default constructor for an FFT_Wrapper object.
     */
    public FFT_Wrapper()
    {
        reset_fft( INITIAL_SIZE );

    }   /* FFT_Wrapper() */


    /**
     * Non-default constructor for an FFT_Wrapper object.
     *
     * @param size
     *        Estimated size of the input array. This can be anything
     *        at first, so long as it's greater than 0. If the size of
     *        the input arrays change, then the FFT functions will
     *        automatically resize the FFT plans.
     */
    public FFT_Wrapper( final int size )
    {
        reset_fft( size );

    }   /* FFT_Wrapper() */


    /**
     * This method resets the FFT plans.
     *
     * @param size
     *        The new size of the FFTs.
     */
    private void reset_fft( final int size )
    {
        n = size;

        f_fft_1d = new FloatFFT_1D( size );
        d_fft_1d = new DoubleFFT_1D( size );

    }   /* reset_fft() */


    /**
     * Computes 1D forward DFT of real data, and returns the result.
     * To get back the original data, use <code>ifft_d</code> on the
     * output of this method.
     *
     * @param in
     * 		  The input array to transform. This can be an array of any size.
     *        <br /><b>NOTE:</b>This buffer shouldn't contain
     *        any imaginary values--just real values.
     *
     * @return
     * 	This function returns an array of doubles. The size of the output
     * 	array is twice the size of the original input. Real numbers are
     * 	stored in the first half of the array, and the imaginary parts
     * 	are stored in the second half of the array. I.e.: <br />
     * 	<pre>
     * 	    a[2*k]   = Re[k],
     * 	    a[2*k+1] = Im[k], 0&lt;=k&lt;n
     * 	</pre>
     */
    public double [] fft( final double [] in )
    {
        /*-------------------------------------
         * Local Variables:
         * 	    i       - For-loop iterator
         *      in_len  - size of input array
         *      out	    - output array
         *-----------------------------------*/
         int        i;
         int        in_len = in.length;
         double []  out    = new double[ in.length << 1 ];

        /*-------------------------------------
         * Check whether the size of the
         * input array matches the size of
         * our FFT plans
         *-----------------------------------*/
        if( in_len != n )
        {
            reset_fft( in_len );
        }

        /*-------------------------------------
         * Initialize the output array
         *-----------------------------------*/
        for( i = 0; i < in_len; ++i )
        {
            out[ i ] = in[ i ];
        }

        /*-------------------------------------
         * Perform transformation, and return
         * the result.
         *-----------------------------------*/
        d_fft_1d.realForwardFull( out );
        return( out );

    }   /* fft() */


    /**
     * Computes 1D forward DFT of real data, and returns the result.
     * To get back the original data, use <code>ifft_f</code> on the
     * output of this method.
     *
     * @param in
     * 	      The input array to transform. This can be an array of any size.
     *        <br /><b>NOTE:</b>This buffer shouldn't contain
     *        any imaginary values--just real values.
     *
     * @return
     * 	This function returns an array of floats. The size of the output
     * 	array is twice the size of the original input. Real numbers are
     * 	stored in the first half of the array, and the imaginary parts
     * 	are stored in the second half of the array. I.e.: <br />
     * 	<pre>
     * 	    a[2*k]   = Re[k],
     * 	    a[2*k+1] = Im[k], 0&lt;=k&lt;n
     * 	</pre>
     */
    public float [] fft( final float [] in )
    {
        /*-------------------------------------
         * Local Variables:
         *      i       - for-loop iterator
         *      in_len  - size of input array
         *      out	    - output array
         *-----------------------------------*/
        int         i;
        int         in_len = in.length;
        float []    out    = new float[ in_len << 1 ];

        /*-------------------------------------
         * Check whether the size of the
         * input array matches the size of
         * our FFT plans
         *-----------------------------------*/
        if( in_len != n )
        {
            reset_fft( in_len );
        }

        /*-------------------------------------
         * Initialize the output array
         *-----------------------------------*/
        for( i = 0; i < in_len; ++i )
        {
            out[ i ] = in[ i ];
        }

        /*-------------------------------------
         * Perform transformation, and return
         * the result.
         *-----------------------------------*/
        f_fft_1d.realForwardFull( out );
        return( out );

    }   /* fft() */


    /**
     * Computes 1D inverse DFT of real data, and returns the result.
     * The physical layout of the input data has to be as follows:<br>
     *
     * <pre>
     *     a[offa+2*k] = Re[k],
     *     a[offa+2*k+1] = Im[k], 0&lt;=k&lt;n
     * </pre>
     *
     * @param in
     *        Data to transform
     *
     * @return
     * 	This function returns an array of floating-point
     * 	values. Layout of the output array is as follows: <br />
     * 	<pre>
     * 	    a[offa+2*k] = Re[k],
     * 	    a[offa+2*k+1] = Im[k], 0&lt;=k&lt;n
     * 	</pre>
     */
    public float [] ifft( final float [] in )
    {
        /*-------------------------------------
         * Local Variables:
         * 	    i       - For-loop iterator
         *      in_len  - size of input array
         *      out	    - output array
         *-----------------------------------*/
        int         i;
        int         in_len = in.length;
        float []    out    = new float[ in_len ];

        /*-------------------------------------
         * Check whether the size of the
         * input array matches the size of
         * our FFT plans
         *-----------------------------------*/
        if( in_len != ( n << 1 ) )
        {
            reset_fft( in_len >> 1 );
        }

        /*-------------------------------------
         * Initialize the output array
         *-----------------------------------*/
        for( i = 0; i < in_len; ++i )
        {
            out[ i ] = in[ i ];
        }

        /*-------------------------------------
         * Perform inverse transformation,
         * and return the result.
         *-----------------------------------*/
        f_fft_1d.complexInverse( out, true );

        return( out );

    }   /* ifft() */


    /**
     * Computes 1D inverse DFT of real data, and returns the result.
     * The physical layout of the input data has to be as follows:<br>
     *
     * <pre>
     * 	   a[offa+2*k] = Re[k],
     * 	   a[offa+2*k+1] = Im[k], 0&lt;=k&lt;n
     * </pre>
     *
     * @param in
     *        Data to transform
     *
     * @return
     * 	This function returns an array of floating-point
     * 	values. Layout of the output array is as follows: <br />
     * 	<pre>
     * 	    a[offa+2*k] = Re[k],
     * 	    a[offa+2*k+1] = Im[k], 0&lt;=k&lt;n
     * 	</pre>
     */
    public double [] ifft( final double [] in )
    {
        /*-------------------------------------
         * Local Variables:
         * 	    i       - For-loop iterator
         *      in_len  - size of input array
         *      out	    - output array
         *-----------------------------------*/
        int         i;
        int         in_len = in.length;
        double []   out    = new double[ in_len ];

        /*-------------------------------------
         * Check whether the size of the
         * input array matches the size of
         * our FFT plans
         *-----------------------------------*/
        if( in_len != ( n << 1 ) )
        {
            reset_fft( in_len >> 1 );
        }

        /*-------------------------------------
         * Initialize the output array
         *-----------------------------------*/
        for( i = 0; i < in_len; ++i )
        {
            out[ i ] = in[ i ];
        }

        /*-------------------------------------
         * Perform inverse transformation,
         * and return the result.
         *-----------------------------------*/
        d_fft_1d.complexInverse( out, true );

        return( out );

    }   /* ifft() */


    /**
     * Computes 1D circular convolution of real data, and returns the result.
     * <br /><b>NOTE:</b>We should probably find a more efficient
     * algorithm for this.
     *
     * @param x
     *        A buffer. <br />
     *        <b>NOTE:</b>This buffer shouldn't contain
     *        any imaginary values--just real values.
     *
     * @param y
     * 	      Another buffer <br />
     *        <b>NOTE:</b>This buffer shouldn't contain
     *        any imaginary values--just real values.
     *
     * @return
     * 	This function returns an array of double
     * 	values. Layout of the output array is as follows: <br />
     * 	<pre>
     * 	    a[offa+2*k] = Re[k],
     * 	    a[offa+2*k+1] = Im[k], 0&lt;=k&lt;n
     * 	</pre>
     */
    public double [] cconvolve( final double [] x,  final double [] y)
    {
        /*-------------------------------------
         * Local Variables:
         * 	    re    - For-loop iterator
         *      im    - indexing variable
         *      len   - size of input arrays
         *      fft_x - Fourier transform of x
         *      fft_y - Fourier transform of y
         *      out   - output array
         *-----------------------------------*/
        int         re;
        int         im;
        int         len = x.length;
        double []   fft_x;
        double []   fft_y;
        double []   out;

        /*-------------------------------------
         * Check whether the input arrays are
         * the same size.
         *-----------------------------------*/
        if( len != y.length )
        {
            throw new RuntimeException( "Dimensions don't agree" );
        }

        /*-------------------------------------
         * Check whether the FFT plan size
         * and the input buffer sizes match
         *-----------------------------------*/
        if( len != n )
        {
            reset_fft( len );
        }

        /*-------------------------------------
         * Transform the input buffers
         *-----------------------------------*/
        fft_x = fft( x );
        fft_y = fft( y );

        /*-------------------------------------
         * Multiply each component.
         *
         * j = a + bi
         * k = c + di
         *
         * jk = (a + bi)(c + di )
         *    =  ac + bd(i^2) + bci + adi
         *    = (ac - bd) + (ad + bc)i
         *
         * Because of the way JTransforms
         * formats its output buffers, the
         * imaginary component is directly
         * following its corresponding real
         * component, so that's what the
         * buffer indexing is doing.
         *-----------------------------------*/
        out = new double[ len << 1 ];
        for ( re = 0; re < ( len << 1 ); re += 2 )
        {
            im = re + 1;
            out[ re ] = ( fft_x[ re ] * fft_y[ re ] )
                      - ( fft_x[ im ] * fft_y[ im ] );
            out[ im ] = ( fft_x[ re ] * fft_y[ im ] )
                      + ( fft_x[ im ] * fft_y[ re ] );
        }

        /*-------------------------------------
         * Perform an inverse transformation
         * and return the output.
         *-----------------------------------*/
        return( ifft( out ) );

    }   /* cconvolve() */


    /**
     * Computes 1D circular convolution of real data, and returns the result.
     * <br /><b>NOTE:</b>We should probably find a more efficient
     * algorithm for this.
     *
     * @param x
     *        A buffer. <br />
     *        <b>NOTE:</b>This buffer shouldn't contain
     *        any imaginary values--just real values.
     *
     * @param y
     *        Another buffer <br />
     *        <b>NOTE:</b>This buffer shouldn't contain
     *        any imaginary values--just real values.
     *
     * @return
     * 	This function returns an array of floating-point
     * 	values. Layout of the output array is as follows: <br />
     * 	<pre>
     * 	    a[offa+2*k] = Re[k],
     * 	    a[offa+2*k+1] = Im[k], 0&lt;=k&lt;n
     * 	</pre>
     */
    public float [] cconvolve( final float [] x, final float [] y)
    {
        /*-------------------------------------
         * Local Variables:
         * 	    re    - For-loop iterator
         *      im    - indexing variable
         *      len   - size of input arrays
         *      fft_x - Fourier transform of x
         *      fft_y - Fourier transform of y
         *      out   - output array
         *-----------------------------------*/
        int         re;
        int         im;
        int         len = x.length;
        float []    fft_x;
        float []    fft_y;
        float []    out;

        /*-------------------------------------
         * Check whether the input arrays are
         * the same size.
         *-----------------------------------*/
        if( len != y.length )
        {
            throw new RuntimeException( "Dimensions don't agree" );
        }

        /*-------------------------------------
         * Check whether the FFT plan size
         * and the input buffer sizes match
         *-----------------------------------*/
        if( len != n )
        {
            reset_fft( len );
        }

        /*-------------------------------------
         * Transform the input buffers
         *-----------------------------------*/
        fft_x = fft( x );
        fft_y = fft( y );

        /*-------------------------------------
         * Multiply each component.
         *
         * j = a + bi
         * k = c + di
         *
         * jk = (a + bi)(c + di )
         *    =  ac + bd(i^2) + bci + adi
         *    = (ac - bd) + (ad + bc)i
         *
         * Because of the way JTransforms
         * formats its output buffers, the
         * imaginary component is directly
         * following its corresponding real
         * component, so that's what the
         * buffer indexing is doing.
         *-----------------------------------*/
        out = new float[ len << 1 ];
        for( re = 0; re < ( len << 1 ); re += 2 )
        {
            im = re + 1;
            out[ re ] = ( fft_x[ re ] * fft_y[ re ] )
                      - ( fft_x[ im ] * fft_y[ im ] );
            out[ im ] = ( fft_x[ re ] * fft_y[ im ] )
                      + ( fft_x[ im ] * fft_y[ re ] );
        }

        /*-------------------------------------
         * Perform an inverse transformation
         * and return the output.
         *-----------------------------------*/
        return( ifft( out ) );

    }   /* cconvolve() */


    /**
     * Computes 1D linear convolution of real data, and returns the result.
     * <br /><b>NOTE:</b>We should probably find a more efficient
     * algorithm for this. I also don't know why we have a circular
     * convolution function call in a linear convolution algorithm.
     *
     * @param x
     *        A buffer. <br />
     *        <b>NOTE:</b>This buffer shouldn't contain
     *        any imaginary values--just real values.
     *
     * @param y
     * 	      Another buffer <br />
     *        <b>NOTE:</b>This buffer shouldn't contain
     *        any imaginary values--just real values.
     *
     * @return
     *  This function returns an array of double
     * 	values. Layout of the output array is as follows: <br />
     * 	<pre>
     * 	    a[offa+2*k] = Re[k],
     * 	    a[offa+2*k+1] = Im[k], 0&lt;=k&lt;n
     * 	</pre>
     */
    public double [] convolve( final double [] x, final double [] y)
    {
        /*-------------------------------------
         * Local Variables:
         *      i - For-loop iterator
         *      a - a buffer
         *      b - another buffer
         *-----------------------------------*/
        int         i;
        double []   a = new double[ x.length << 1 ];
        double []   b = new double[ y.length << 1 ];

        /*-------------------------------------
         * Copy the "x" buffer to the "a"
         * buffer and pad the rest of the
         * buffer with zeroes.
         *-----------------------------------*/
        for( i = 0; i < x.length; ++i )
        {
            a[ i ] = x[ i ];
        }
        for( i = x.length; i < ( x.length << 1 ); ++i )
        {
            a[ i ] = 0.0;
        }

        /*-------------------------------------
         * Copy the "y" buffer to the "b"
         * buffer and pad the rest of the
         * buffer with zeroes.
         *-----------------------------------*/
        for( i = 0; i < y.length; ++i )
        {
            b[ i ] = y[ i ];
        }
        for( i = y.length; i < ( y.length << 1 ); ++i )
        {
            b[ i ] = 0.0;
        }

        /*-------------------------------------
         * Circular convolution?
         *-----------------------------------*/
        return( cconvolve( a, b ) );

    }   /* convolve() */


    /**
     * Computes 1D linear convolution of real data, and returns the result.
     * <br /><b>NOTE:</b>We should probably find a more efficient
     * algorithm for this. I also don't know why we have a circular
     * convolution function call in a linear convolution algorithm.
     *
     * @param x
     *        A buffer. <br />
     *        <b>NOTE:</b>This buffer shouldn't contain
     *        any imaginary values--just real values.
     *
     * @param y
     * 	      Another buffer <br />
     *        <b>NOTE:</b>This buffer shouldn't contain
     *        any imaginary values--just real values.
     *
     * @return
     * 	This function returns an array of floating-pont
     * 	values. Layout of the output array is as follows: <br />
     * 	<pre>
     * 	    a[offa+2*k] = Re[k],
     * 	    a[offa+2*k+1] = Im[k], 0&lt;=k&lt;n
     * 	</pre>
     */
    public float [] convolve( final float [] x, final float [] y)
    {
        /*-------------------------------------
         * Local Variables:
         * 	    i - For-loop iterator
         *      a - a buffer
         *      b - another buffer
         *-----------------------------------*/
        int         i;
        float []    a = new float[ x.length << 1 ];
        float []    b = new float[ y.length << 1 ];

        /*-------------------------------------
         * Copy the "x" buffer to the "a"
         * buffer and pad the rest of the
         * buffer with zeroes.
         *-----------------------------------*/
        for( i = 0; i < x.length; ++i )
        {
            a[ i ] = x[ i ];
        }
        for( i = x.length; i < ( x.length << 1 ); ++i )
        {
            a[ i ] = 0.0f;
        }

        /*-------------------------------------
         * Copy the "y" buffer to the "b"
         * buffer and pad the rest of the
         * buffer with zeroes.
         *-----------------------------------*/
        for( i = 0; i < y.length; ++i )
        {
            b[ i ] = y[ i ];
        }
        for( i = y.length; i < ( y.length << 1 ); ++i )
        {
            b[ i ] = 0.0f;
        }

        /*-------------------------------------
         * Circular convolution?
         *-----------------------------------*/
        return( cconvolve( a, b ) );

    }   /* convolve() */


    /**
     * Prints the contents of a buffer to the screen. As of
     * right now, this function can only correctly print
     * buffers where the data is represented in the
     * following form: <br />
     * <pre>
     *     a[offa+2*k] = Re[k],
     *     a[offa+2*k+1] = Im[k], 0&lt;=k&lt;n
     * </pre>
     *
     * @param x
     *        A buffer.
     *
     * @param title
     *        A title which will be printed above the
     *        buffer's data table.
     */
    public void show( final double [] x, final String title )
    {
        /*-------------------------------------
         * Local Variables:
         * 	    im 	    - indexing variable
         * 	    EPSILON - used for comparing
         *				  floating-point values
         *-----------------------------------*/
        int     im;
        double  EPSILON = 1.0E-10;

        /*-------------------------------------
	     * Print out the table
	     *-----------------------------------*/
        System.out.println( title );
        System.out.println( "-------------------" );
        for (int i = 0; i < x.length; i += 2)
        {
            im = i + 1;
            if( ( x[ im ] < EPSILON ) && ( x[ im ] > -EPSILON ) )
            {
                System.out.println( x[ i ] );
            }
            else if( x[ im ] > 0 )
            {
                System.out.println( x[ i ] + " + " + x[ im ] + "i" );
            }
            else
            {
                System.out.println( x[ i ] + " - " + -x[ im ] + "i" );
            }
        }
        System.out.println();

    }   /* show() */


    /**
     * Prints the contents of a buffer to the screen. As of
     * right now, this function can only correctly print
     * buffers where the data is represented in the
     * following form: <br />
     * <pre>
     *     a[offa+2*k] = Re[k],
     *     a[offa+2*k+1] = Im[k], 0&lt;=k&lt;n
     * </pre>
     *
     * @param x
     *        A buffer.
     *
     * @param title
     *        A title which will be printed above the
     *        buffer's data table.
     */
    public void show( final float [] x, final String title )
    {
        /*-------------------------------------
         * Local Variables:
         *      im 	    - indexing variable
         * 	    EPSILON - used for comparing
         *	              floating-point values
         *-----------------------------------*/
        int     im;
        float   EPSILON = 1.0E-10f;

        /*-------------------------------------
         * Print out the table
         *-----------------------------------*/
        System.out.println( title );
        System.out.println( "-------------------" );
        for (int i = 0; i < x.length; ++i)
        {
            im = i + 1;
            if( ( x[ im ] < EPSILON ) && ( x[ im ] > -EPSILON ) )
            {
                System.out.println( x[ i ] );
            }
            else if( x[ im ] > 0 )
            {
                System.out.println( x[ i ] + " + " + x[ im ] + "i" );
            }
            else
            {
                System.out.println( x[ i ] + " - " + -x[ im ] + "i" );
            }
        }
        System.out.println();

    }   /* show() */


    /*********************************************************************
     *  Test client and sample execution
     *
     *  Output from this FFT implementation:
     *
     *  x
     *  -------------------
     *  -0.03480425839330703
     *  0.07910192950176387
     *  0.7233322451735928
     *  0.1659819820667019
     *
     *  y = fft(x)
	 *  -------------------
	 *  0.9336118983487516
	 *  -0.7581365035668999 + 0.08688005256493803i
	 *  0.44344407521182005
	 *  -0.7581365035668999 - 0.08688005256493803i
	 *
	 *  z = ifft(y)
	 *  -------------------
	 *  -0.03480425839330703
	 *  0.07910192950176387
	 *  0.7233322451735928
	 *  0.1659819820667019
	 *
	 *  d = cconv(x)
	 *  -------------------
	 *  0.5506798633981853
	 *  0.23461407150576394
	 *  -0.016542951108772352
	 *  0.10288019294318276
	 *
	 *  d = conv(x)
	 *  -------------------
	 *  0.001211336402308083
	 *  -0.0055061679875771374
	 *  -0.04409296947956329
	 *  0.10288019294318279
	 *  0.5494685269958772
	 *  0.24012023949334108
	 *  0.027550018370790935
	 *  -3.469446951953614E-17
     *
     *
     *  Output from previous FFT implementation tests:
     *
     *  x
     *  -------------------
     *  -0.03480425839330703
     *  0.07910192950176387
     *  0.7233322451735928
     *  0.1659819820667019
     *
     *  y = fft(x)
     *  -------------------
     *  0.9336118983487516
     *  -0.7581365035668999 + 0.08688005256493803i
     *  0.44344407521182005
     *  -0.7581365035668999 - 0.08688005256493803i
     *
     *  z = ifft(y)
     *  -------------------
     *  -0.03480425839330703
     *  0.07910192950176387 + 2.6599344570851287E-18i
     *  0.7233322451735928
     *  0.1659819820667019 - 2.6599344570851287E-18i
     *
     *  c = cconvolve(x, x)
     *  -------------------
     *  0.5506798633981853
     *  0.23461407150576394 - 4.033186818023279E-18i
     *  -0.016542951108772352
     *  0.10288019294318276 + 4.033186818023279E-18i
     *
     *  d = convolve(x, x)
     *  -------------------
     *  0.001211336402308083 - 3.122502256758253E-17i
     *  -0.005506167987577068 - 5.058885073636224E-17i
     *  -0.044092969479563274 + 2.1934338938072244E-18i
     *  0.10288019294318276 - 3.6147323062478115E-17i
     *  0.5494685269958772 + 3.122502256758253E-17i
     *  0.240120239493341 + 4.655566391833896E-17i
     *  0.02755001837079092 - 2.1934338938072244E-18i
     *  4.01805098805014E-17i
     *
     *
     *
     *  As you can see, there are only a few differences between
     *  these two implementations. The most obvious difference
     *  is that the new implementation says that extremely
     *  small imaginary values are zero. In some ways, this
   	 *  can be seen as more accurate, since, in the case of
   	 *  the inverse transform, you actually get the exact values
   	 *  back if they didn't have imaginary values to begin with.
   	 *
   	 *  If you compare the final entry of the convolve() tables,
   	 *  you'll also notice another small difference: the value
   	 *  given by the newer implementation is actually smaller
   	 *  than the result given by the old implementation. This
   	 *  might be attributed to precision issues, but since both
   	 *  values are extremely small, the difference should be
   	 *  negligible.
   	 *
     *********************************************************************/
    /*
    public static void main( String[] args )
    {
        FFT_Wrapper temp = new FFT_Wrapper();
        double [] data = new double[ 4 ];
        double [] out;

        data[ 0 ] = ( -0.03480425839330703 );
        data[ 1 ] = (  0.07910192950176387 );
        data[ 2 ] = (  0.7233322451735928  );
        data[ 3 ] = (  0.1659819820667019  );
        System.out.println( "x" );
        System.out.println( "-------------------" );
        for (int i = 0; i < data.length; ++i)
        {
            System.out.println( data[ i ] );
        }
        System.out.println();

        out = temp.fft(data);
        temp.show(out, "y = fft(x)");

        double [] bob = temp.ifft( out );
        temp.show(bob, "z = ifft(y)");

        double [] cconv = temp.cconvolve(data, data);
        temp.show(cconv, "d = cconv(x)");

        double [] conv = temp.convolve(data, data);
        temp.show(conv, "d = conv(x)");
    }*/	/* main() */

}   /* FFT_Wrapper */
