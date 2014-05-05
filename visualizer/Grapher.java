import java.awt.*;
import javax.swing.*;
import javax.swing.Box;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

/**
 * Manages all of the graph panels
 */
public class Grapher extends JPanel
{
    private volatile boolean updating;
    
    private GraphBox inverse;
    private GraphBox original;
    private GraphBox mixed;
    private GraphBox all;
    
    /**
     * Default constructor
     */
    public Grapher()
    {
        super();
        updating = false;
        inverse = new GraphBox( "Inverted Wave", Color.green, 0.5f, 0.125f );
        original = new GraphBox( "Original Wave", Color.blue, 0.5f, 0.125f );
        mixed = new GraphBox( "Sum of the Inverted and Original Waves", Color.red, 1.0f, 0.125f );
        all = new GraphBox( "EVERYTHING EVER", 1.0f, 0.25f, original.getGraphData()[ 0 ], inverse.getGraphData()[ 0 ], mixed.getGraphData()[ 0 ] );
        
        JPanel temp1 = new JPanel();
        GridLayout inv_orig_box = new GridLayout(1, 2, 5, 5);
        temp1.setLayout( inv_orig_box );
        temp1.add( original );
        temp1.add( inverse );
        
        JPanel temp2 = new JPanel();
        GridLayout mixed_box = new GridLayout(2, 1, 5, 5);
        temp2.setLayout( mixed_box );
        temp2.add( temp1 );
        temp2.add( mixed );
      
        GridLayout main_box = new GridLayout(2, 1, 5, 5);
        this.setLayout( main_box );
        this.add( temp2 );
        this.add( all );
    }
    
    /**
     * This name should be changed.
     * 
     * This function updates the graphs'
     * values so that we get a new picture
     * during the next render call.
     * 
     * @param orig
     *  Buffer containing the original mic data
     * 
     * @param inv
     *  Buffer containing the inverted data
     */
    public void graph( short [] orig, short [] inv )
    {
        int i;
        short [] temp_buf = new short[ orig.length ];
        
        if( updating ) 
        {
            return;
        }
        
        updating = true;
        
        inverse.updateGraph( orig );
        original.updateGraph( inv );
        
        for( i = 0; i < temp_buf.length; ++i )
        {
            temp_buf[ i ] = (short)( orig[ i ] + inv[ i ] );
        }
        
        mixed.updateGraph( temp_buf );
        all.updateGraph( orig, inv, temp_buf );
        
        updating = false;
        
        //repaint();
    }
    
    /**
     * Tells the graphs to repaint
     */
    @Override
    public void paintComponent( Graphics g )
    {
        super.paintComponent(g);
        
        while(updating);
        updating = true;
        
        inverse.repaint();
        original.repaint();
        mixed.repaint();
        all.repaint();
        
        updating = false;
    }
};

/**
 * A simple buffer class. I originally wanted
 * to use generics, but I couldn't get it
 * to work without making things super ugly.
 * And unsafe.
 */
class Buffer
{
    private short [] buf;
    private int cur_idx;
    private int data_size;
    
    /**
     * non-default constructor
     * 
     * @param size
     *  The new size of the buffer
     * 
     * @param step
     *  I totally forgot what this does. 
     *  I guess that it gives us some extra space
     */
    public Buffer( int size, int step )
    {
        cur_idx = 0;
        data_size = size;
        buf = new short[ size * step ];
        for( int i = 0; i < buf.length; ++i )
        {
            buf[ i ] = 0;
        }
    }
    
    /**
     * Returns the element at index idx
     * 
     * @param idx
     *  The index of the element we want to access
     * 
     * @return
     *  Returns the element at index idx
     * 
     * @throws RuntimeException
     */
    public short at( int idx ) throws RuntimeException
    {
        if( idx >= buf.length )
        {
            throw new RuntimeException();
        }
        
        return( buf[ idx ] );
    }
    
    /**
     * Adds an element to the end of the
     * buffer. If the length is
     * exceeded, then elements are
     * removed from the front of the buffer.
     * 
     * @param new_elem
     *  New element to add
     */
    public void add( short new_elem )
    {
        int i;
        if( cur_idx == buf.length )
        {
            for( i = 0; i < buf.length - data_size; ++i )
            {
                buf[ i ] = buf[ i + data_size ];
            }
            
            for( i = buf.length - data_size; i < buf.length; ++i )
            {
                buf[ i ] = 0;
            }
            
            cur_idx = buf.length - data_size;
        }
        
        buf[ cur_idx++ ] = new_elem;
        
    }
    
    /**
     * Returns the total length of the buffer
     * 
     * @return
     *  Returns the length of the buffer
     */
    public int length()
    {
        return( buf.length );
    }
};

/**
 * A class having all of the necessary components for
 * a graph: a set of points, and the color of
 * the liine.
 */
class GraphData
{
    private volatile Buffer points;
    private Color color;
    
    /**
     * Default constructor
     */
    public GraphData()
    {
        points = null;
        color = Color.black;
    }
    
    /**
     * Non-default constructor
     * 
     * @param new_color
     *  The color of this graph's line
     */
    public GraphData( Color new_color )
    {
        points = null;
        color = new_color;
    }
    
    /**
     * Returns the points buffer
     * 
     * @return
     *  Returns the points buffer
     */
    public Buffer getPoints()
    {
        return( points );
    }
    
    /**
     * Returns the color
     * 
     * @return 
     *  Returns the color
     */
    public Color getColor()
    {
        return( color );
    }
    
    /**
     * Returns the point at index idx
     * 
     * @param idx
     *  Index of the element we're interested in
     *  
     * @return
     *  Returns the point at index idx
     */
    public short getPoint( int idx )
    {
        return( points.at( idx ) );
    }
    
    /**
     * Returns the number of points this graph has
     * 
     * @return
     *  Returns the number of points this graph has
     */
    public int numPoints()
    {
        if( null == points )
        {
            return( 0 );
        }
        
        return( points.length() );
    }
    
    /**
     * Updates the points in the buffer to
     * include new points from data
     * 
     * @param data
     *  New points to add to the buffer
     */
    public void updatePoints( short [] data )
    {
        if( null == points )
        {
            points = new Buffer( data.length, 2 );
        }
        
        for( int i = 0; i < data.length; ++i )
        {
            points.add( data[ i ] );
        }
    }
}

/**
 * This class acts as a graph container.
 * It holds a graph and its label.
 */
class GraphBox extends JPanel
{
    private Graph graph;
    
    /**
     * Non-default constructor
     * 
     * @param label
     *  Label of the graph
     * 
     * @param color
     *  Color of the graph's line
     * 
     * @param xscale
     *  Scaling on the x-axis (original: 10px)
     * 
     * @param yscale
     *  Scaling on the y-axis
     */
    public GraphBox( String label, Color color, float xscale, float yscale )
    {        
        super();
        graph = new Graph( color, xscale, yscale );
        initSelf( label );
    }
    
    /**
     * Non-default constructor
     * 
     * @param label
     *  Label of the graph
     * 
     * @param xscale
     *  Scaling on the x-axis (original: 10px)
     * 
     * @param yscale
     *  Scaling on the y-axis
     * 
     * @param graphs
     *  Different lines that we want to be drawn 
     *  on this graph
     */
    public GraphBox( String label, float xscale, float yscale, GraphData ... graphs )
    {
        super();
        graph = new Graph( xscale, yscale, graphs );   
        initSelf( label );
    }
    
    /**
     * Creates a label and adds everything to
     * a layout box thing
     * 
     * @param label
     *  Label of the graph
     */
    private void initSelf( String label )
    {
        JLabel l = new JLabel( label );
        GridBagLayout box = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        
        this.setLayout( box );
                
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.weighty = 0;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        box.setConstraints( l, c );
        this.add( l );
        
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridheight = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.LINE_START;
        c.weightx = 1.0;
        c.weighty = 1.0;
        box.setConstraints( graph, c );
        this.add( graph );
    }
    
    /**
     * Updates the graph with new data
     * 
     * @param data
     *  New data with which to update the graph
     */
    public void updateGraph( short [] ... data )
    {        
        graph.fillBuffer( data );
    }
    
    /**
     * Repaints the graph
     * 
     * @param g
     *  A graphics context
     */
    @Override
    public void paintComponent( Graphics g )
    {
        super.paintComponent( g );
        graph.repaint();
    }
    
    /**
     * Returns the this graph's graph data
     * 
     * @return
     *  Returns this graph's graph data
     */
    public GraphData [] getGraphData()
    {
        return( graph.getGraphData() );
    }
};

/**
 * THIS is the actual graph
 */
class Graph extends JPanel
{
    private GraphData [] data;
    private volatile boolean updating;
    private float hscale;
    private float vscale;
    
    /**
     * Non-default constructor
     * 
     * @param color
     *  Color of the graph's line
     * 
     * @param xscale
     *  Scaling on the x-axis (original: 10px)
     * 
     * @param yscale
     *  Scaling on the y-axis
     */
    public Graph( Color color, float xscale, float yscale )
    {
        super();
        updating = false;
        hscale = xscale;
        vscale = yscale;
        initGraphData( new GraphData( color ) );
    }
    
    /**
     * Non-default constructor
     * 
     * @param color
     *  Color of the graph's line
     */
    public Graph( Color color )
    {
        super();
        updating = false;
        hscale = 1.0f;
        vscale = 1.0f;
        initGraphData( new GraphData( color ) );
    }
    
    /**
     * Non-default constructor
     * 
     * @param xscale
     *  Scaling on the x-axis (original: 10px)
     * 
     * @param yscale
     *  Scaling on the y-axis
     */
    public Graph( float xscale, float yscale )
    {
        super();
        updating = false;
        hscale = xscale;
        vscale = yscale;
        initGraphData( new GraphData() );
    }
    
    /**
     * Non-default constructor
     * 
     * @param xscale
     *  Scaling on the x-axis (original: 10px)
     * 
     * @param yscale
     *  Scaling on the y-axis
     *  
     * @param graphs
     *  This graph's graph data
     */
    public Graph( float xscale, float yscale, GraphData ... graphs )
    {
        super();
        updating = false;
        hscale = xscale;
        vscale = yscale;
        initGraphData( graphs );
    }
    
    /**
     * Initializes the graph data of the graph
     * 
     * @param graphs
     *  The data to which we initialize
     */
    private void initGraphData( GraphData ... graphs )
    {
        int idx = 0;
        data = new GraphData[ graphs.length ];
        for( GraphData dat : graphs )
        {
            data[ idx ] = dat;
            ++idx;
        }
    }
    
    /**
     * Updates the graph's point buffer with new points
     * 
     * @param dat
     *  Data used to update the graph's points.
     */
    public void fillBuffer( short [] ... dat )
    {
        int idx = 0;
        if( updating )
        {
            return;
        }
        
        updating = true;
        
        for( short [] points : dat )
        {
            data[ idx ].updatePoints( points );
            ++idx;
        }
        
        updating = false;
    }
    
    /**
     * Grabs the graph's graph data
     * 
     * @return
     *  Returns the graph's graph data
     */
    public GraphData [] getGraphData()
    {
        return( data );
    }
    
    /**
     * Paints the graph
     * 
     * @param g
     *  The graphics context
     */
    @Override
    public void paintComponent( Graphics g )
    {
        int h = getHeight() >> 1;
        super.paintComponent(g);
        
        if( updating )
        {
            return;
        }
        
        updating = true;
        
        /* Clear the screen */
        g.setColor( Color.lightGray );
        g.fillRect(0, 0, getWidth(), getHeight());
        
        /* Draw horizontal axis */
        g.setColor( Color.black );
        g.drawLine(0, h, getWidth(), h);
        
        /* Draw actual wave */
        for( GraphData line : data )
        {
            g.setColor( line.getColor() );
            for( int i = 0; i < line.numPoints() - 1; ++i )
            {
                g.drawLine( (int)( hscale * 10 * i ), h - (int)( vscale * line.getPoint( i ) ), (int)( hscale * 10 * ( i + 1 ) ), h - (int)( vscale * line.getPoint( i + 1 ) ) );
            }
        }
        
        updating = false;
    }
};
