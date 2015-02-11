package com.lescomber.vestige.map;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.crossover.SpriteManager.SpriteTemplate;
import com.lescomber.vestige.framework.Screen;
import com.lescomber.vestige.framework.Util;
import com.lescomber.vestige.geometry.Line;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.graphics.Sprite;
import com.lescomber.vestige.units.AIUnit;
import com.lescomber.vestige.units.Player;

public class Map
{
	private final int stageNum;
	private final int levelNum;
	
	private boolean bossLevel;
	
	// Background tile fields
	private final Sprite[][] backgroundTiles;
	private static final int TILE_ROWS = 5;
	private static final int TILE_COLS = 10;
	private static final int NUM_TILES = 5;
	private static final int TILE_WIDTH = 80;
	private static final int TILE_HEIGHT = 96;
	
	private final ArrayList<Obstacle> obstacles;
	
	int nodesArraySize;
	MapNode[] nodes;
	
	private final Point playerSpawnPoint;
	private final ArrayList<Point> enemySpawnPoints;
	private final ArrayList<Integer> availableSpawnPoints;
	private final ArrayList<Integer> spawnPointCDs;
	private static final int SPAWN_POINT_CD = 200;
	
	private final Portal portal;
	
	private final LinkedList<Wave> upcomingWaves;
	private Wave currentWave;
	private int waveCount;
	private static final int MAX_WAVE_GAP = 4000;	// Max time between waves (in ms). If a wave finishes "early", the next
													//wave will spawn at most MAX_WAVE_GAP later
	
	private final ArrayList<AIUnit> enemiesBuffer;
	private final ArrayList<AIUnit> enemiesReady;
	
	public static final float PLAYER_HALF_WIDTH = 24.5f;	// playerHalfWidth
	public static final float PLAYER_HALF_HEIGHT = 19.5f;	// playerHalfHeight
	
	public Map(int stageNum, int levelNum)
	{
		this.stageNum = stageNum;
		this.levelNum = levelNum;
		
		bossLevel = false;
		
		backgroundTiles = new Sprite[TILE_ROWS][TILE_COLS];
		setBackgroundTiles(randomBackgroundTiles());
		
		nodesArraySize = 0;
		obstacles = new ArrayList<Obstacle>();
		playerSpawnPoint = new Point(200, 240);
		enemySpawnPoints = new ArrayList<Point>(5);
		availableSpawnPoints = new ArrayList<Integer>(5);
		spawnPointCDs = new ArrayList<Integer>(5);
		
		portal = new Portal(0, 0);
		
		upcomingWaves = new LinkedList<Wave>();
		currentWave = null;
		waveCount = 0;
		
		enemiesBuffer = new ArrayList<AIUnit>(5);
		enemiesReady = new ArrayList<AIUnit>(5);
	}
	
	public void setBackgroundTiles(int[][] tileNums)
	{
		if (tileNums.length != TILE_ROWS || tileNums[0].length != TILE_COLS)
			return;
		
		final float startX = TILE_WIDTH / 2;
		final float startY = (TILE_HEIGHT / 2) - 3;
		
		for (int i=0; i<TILE_ROWS; i++)
			for (int j=0; j<TILE_COLS; j++)
			{
				final SpriteTemplate template = SpriteManager.tiles[tileNums[i][j]];
				final float x = startX + j * TILE_WIDTH;
				final float y = startY + i * TILE_HEIGHT;
				backgroundTiles[i][j] = new Sprite(template, x, y);
			}
	}
	
	private int[][] randomBackgroundTiles()
	{
		final int[][] randomTiles = new int[TILE_ROWS][TILE_COLS];
		
		for (int i=0; i<TILE_ROWS; i++)
			for (int j=0; j<TILE_COLS; j++)
			{
				randomTiles[i][j] = Util.rand.nextInt(NUM_TILES);
			}
		
		return randomTiles;
	}
	
	public void update(int deltaTime)
	{
		if (portal.isVisible())
			portal.update(deltaTime);
		
		// Update enemy spawn point cooldowns
		for (int i=0; i<spawnPointCDs.size(); i++)
		{
			if (spawnPointCDs.get(i) > 0)
			{
				final int newCD = spawnPointCDs.get(i) - deltaTime;
				spawnPointCDs.set(i, newCD);
				if (newCD <= 0)
					availableSpawnPoints.add(i);
			}
		}
		
		// Update wave countdowns and trigger next wave if applicable
		if (!upcomingWaves.isEmpty())
		{
			if (upcomingWaves.get(0).updateWaveCountdown(deltaTime))
			{
				// TODO: Add next wave to current wave? Otherwise any remaining units from current wave get ignored, but should they?
				currentWave = upcomingWaves.remove();
				if (!upcomingWaves.isEmpty())
					upcomingWaves.get(0).addToWaveCountdown(currentWave.getWaveCountdown());
			}
		}
		
		// Update current wave and spawn any units that are ready to go
		currentWave.update(deltaTime);
		for (final AIUnit aiu : currentWave.getEnemiesQueue())
			spawnUnit(aiu);
	}
	
	private void spawnUnit(AIUnit unit)
	{
		int spawnIndex = -1;
		
		// Case: all spawn points are on cooldown... so we pick the one with the lowest cooldown...
		if (availableSpawnPoints.isEmpty())
		{
			spawnIndex = 0;
			for (int i=1; i<spawnPointCDs.size(); i++)
			{
				if (spawnPointCDs.get(i) < spawnPointCDs.get(spawnIndex))
					spawnIndex = i;
			}
		}
		else
		{
			final int availIndex = Util.rand.nextInt(availableSpawnPoints.size());
			spawnIndex = availableSpawnPoints.remove(availIndex);
		}
		
		spawnPointCDs.set(spawnIndex, SPAWN_POINT_CD);
		final Point spawnPoint = enemySpawnPoints.get(spawnIndex);
		unit.offsetTo(spawnPoint);
		
		// Set newUnit's destination to somewhere on screen (if currently off screen)
		float xDest = unit.getX();
		float yDest = unit.getY();
		final float unitHalfWidth = unit.getHitbox().getWidth() / 2;
		final float unitHalfHeight = unit.getHitbox().getHeight() / 2;
		
		if (xDest < unitHalfWidth)
			xDest = unitHalfWidth;
		else if (xDest > Screen.WIDTH - unitHalfWidth)
			xDest = Screen.WIDTH - unitHalfWidth;
		if (yDest < unitHalfHeight)
			yDest = unitHalfHeight;
		else if (yDest > Screen.HEIGHT - unitHalfHeight)
			yDest = Screen.HEIGHT - unitHalfHeight;
		
		if (xDest != unit.getX() || yDest != unit.getY())
		{
			unit.setDestination(adjustDestination(xDest, yDest));
			unit.setEntering(true);
		}
		
		queueStevesUnit(unit);
	}
	
	// Note: playerSpawnPoint should be set before calling buildNodeNetwork in order to facilitate node pruning
	public void buildNodeNetwork()
	{
		// Build boundaries around obstacles for diverting player destinations when the player's chosen destination
		//would collide with that obstacle. Also used for determining pathing nodes
		buildBoundaries();
		
		ArrayList<Point> pointList = new ArrayList<Point>();
		
		// Retrieve obstacle corner points
		for (final Obstacle o : obstacles)
			pointList.addAll(o.getCorners());
		
		// Prune nodes that are too near the outside of the playing area and prune duplicate nodes
		final ArrayList<Point> temp = new ArrayList<Point>();
		for (final Point p : pointList)
		{
			if (p.x > PLAYER_HALF_WIDTH && p.x < (Screen.WIDTH - PLAYER_HALF_WIDTH) &&
				p.y > PLAYER_HALF_HEIGHT && p.y < (Screen.HEIGHT - PLAYER_HALF_HEIGHT) && !temp.contains(p))
					temp.add(p);
		}
		pointList = temp;
		
		// Establish size of map node array (which should no longer be changing)
		nodesArraySize = pointList.size();
		
		// Create map nodes from the point list and move them into their array
		nodes = new MapNode[nodesArraySize];
		for (int i=0; i<nodesArraySize; i++)
			nodes[i] = new MapNode(pointList.get(i));
		
		// Establish neighbors and their distances
		initNeighbors();
		
		// Complete each node's map of all other nodes including total distance to each
		initDistances();
		
		//=========================
		// Prune nodes that cannot be reached by the player
		//=========================
		
		// Create temporary node at player spawn point and calculate its neighbors/distances
		MapNode testNode = new MapNode(new Point(playerSpawnPoint));
		findNeighbors(testNode);
		for (final int i : testNode.neighbors)
			updateDistances(testNode, i);
		
		// Remove nodes that cannot be reached by any path from player spawn point
		final ArrayList<MapNode> tempList = new ArrayList<MapNode>(nodesArraySize);
		for (int i=0; i<nodesArraySize; i++)
			if (testNode.distances[i] != Integer.MAX_VALUE)
				tempList.add(nodes[i]);
		nodesArraySize = tempList.size();
		nodes = new MapNode[nodesArraySize];
		for (int i=0; i<nodesArraySize; i++)
			nodes[i] = tempList.get(i);
		
		// Rebuild node neighbors/distances now that some nodes have potentially been pruned
		initNeighbors();
		initDistances();
		
		// Prune boundaries that cannot be reached (ie. have no neighboring nodes)
		if (nodesArraySize > 0)
		{
			for (final Obstacle o : obstacles)
			{
				// Make a copy of this obstacle's boundaryLines
				final ArrayList<BoundaryLine> blCopy = new ArrayList<BoundaryLine>();
				for (final BoundaryLine bl : o.getBoundaryLines())
					blCopy.add(bl);
				
				// Pick an arbitrary point on this boundary line and check its neighbors. If it has no neighbors then
				//it can be pruned
				for (final BoundaryLine blc : blCopy)
				{
					testNode = new MapNode(blc.getClosestToPoint(new Point(0, 0)));	// Pick any point on boundary line
					findNeighbors(testNode);
					if (testNode.neighbors.isEmpty())
						o.removeBoundaryLine(blc);
				}
			}
		}
	}
	
	private void buildBoundaries()
	{
		for (final Obstacle o : obstacles)
			o.buildBoundaries(obstacles);
	}
	
	private void findNeighbors(MapNode node)
	{
		for (int i=0; i<nodesArraySize; i++)
		{
			final Line line = new Line(node.location, nodes[i].location);
			if (!lineObstacleCollision(line))
			{
				node.neighbors.add(i);
				node.distances[i] = (int)line.getLength();
			}
		}
	}
	
	private void initNeighbors()
	{
		for (int i=0; i<nodesArraySize; i++)
		{
			for (int j=i+1; j<nodesArraySize; j++)
			{
				final Line line = new Line(nodes[i].location, nodes[j].location);
				if (!lineObstacleCollision(line))
				{
					final int distance = (int)Math.round(line.getLength());
					nodes[i].neighbors.add(j);
					nodes[i].distances[j] = distance;
					nodes[j].neighbors.add(i);
					nodes[j].distances[i] = distance;
				}
			}
		}
	}
	
	private void initDistances()
	{
		for (int i=0; i<nodesArraySize; i++)
			for (final int j : nodes[i].neighbors)
				updateDistances(nodes[i], j);
	}
	
	private void updateDistances(MapNode node, int currentIndex)
	{
		for (final int i : nodes[currentIndex].neighbors)
		{
			final int nDist = node.distances[currentIndex] + nodes[currentIndex].distances[i];
			if (nDist < node.distances[i])
			{
				node.distances[i] = nDist;
				updateDistances(node, i);
			}
		}
	}
	
	private boolean lineObstacleCollision(Line line)
	{
		for (final Obstacle o : obstacles)
			if (o.intersectsPath(line))
				return true;
		
		return false;
	}
	
	public List<Point> getPath(Point start, Point end/*, float topGap*/)
	{
		// Adjust startpoint if needed (and able). Note: shoudn't be needed, but should prevent
		//units from getting stuck on obstacles
		final Point startPoint = adjustDestination(start/*, topGap*/);
		
		// Adjust endpoint if needed (and able)
		final Point endPoint = adjustDestination(end/*, topGap*/);
		if (endPoint == null)
			return new ArrayList<Point>(1);
		
		final ArrayList<Point> path = new ArrayList<Point>();
		
		// Check if a direct path to the destination is possible
		final Line line = new Line(start, endPoint);
		if (!lineObstacleCollision(line))
		{
			path.add(endPoint);
			return path;
		}
		
		// Establish endNode and add it (temporarily) to the node network
		final MapNode endNode = new MapNode(endPoint);
		findNeighbors(endNode);
		for (final int i : endNode.neighbors)
			updateDistances(endNode, i);
		
		// Ensure we have a valid destination, return empty array if not to avoid getting stuck
		if (endNode.neighbors.size() == 0)
			return new ArrayList<Point>(1);
		
		// Establish startNode and find its neighbors and their distances (but not distances to every other node)
		final MapNode startNode = new MapNode(startPoint);
		findNeighbors(startNode);
		
		// Find the appropriate startNode neighbor and establish total distance to be traveled
		int curIndex = -1;
		int totalDistance = Integer.MAX_VALUE;
		for (final int i : startNode.neighbors)
		{
			final int dist = startNode.distances[i] + endNode.distances[i];
			if (dist < totalDistance)
			{
				totalDistance = dist;
				curIndex = i;
			}
		}
		
		// If no path has been found, abort
		if (curIndex < 0)
			return new ArrayList<Point>(1);
		
		// Build the path starting with the already established neighbor, and ending with the endNode
		path.add(nodes[curIndex].location);
		int g = startNode.distances[curIndex];
		while(!endNode.neighbors.contains(curIndex))
		{
			for (final int i : nodes[curIndex].neighbors)
			{
				if (g + nodes[curIndex].distances[i] + endNode.distances[i] == totalDistance)
				{
					g += nodes[curIndex].distances[i];
					path.add(nodes[i].location);
					curIndex = i;
					break;
				}
			}
		}
		path.add(endNode.location);
		
		return path;
	}
	
	public Point adjustDestination(float destX, float destY, float topGap)
	{
		Point p = new Point(destX, destY);
		
		// Check that endpoint is in playable area and adjust its location if needed
		if (p.x < PLAYER_HALF_WIDTH)
			p.x = PLAYER_HALF_WIDTH;
		else if (p.x > Screen.WIDTH - PLAYER_HALF_WIDTH)
			p.x = Screen.WIDTH - PLAYER_HALF_WIDTH;
		if (p.y < PLAYER_HALF_HEIGHT + topGap)
			p.y = PLAYER_HALF_HEIGHT + topGap;
		else if (p.y > Screen.HEIGHT - PLAYER_HALF_HEIGHT)
			p.y = Screen.HEIGHT - PLAYER_HALF_HEIGHT;
		
		// Check for point being inside or too near obstacles and adjust its location if needed
		for (final Obstacle o : obstacles)
			if (o.containsPathPoint(p))
				p = o.adjustPathPoint(p);
		
		return p;
	}
	
	public Point adjustDestination(Point point, float topGap)
	{
		return adjustDestination(point.x, point.y, topGap);
	}
	
	public Point adjustDestination(float destX, float destY)
	{
		return adjustDestination(destX, destY, 0);
	}
	
	public Point adjustDestination(Point point)
	{
		return adjustDestination(point.x, point.y, 0);
	}
	
	public void addWave(Wave wave)
	{
		waveCount++;
		
		if (currentWave == null)
			currentWave = wave;
		else
			upcomingWaves.add(wave);
	}
	
	public void gameScreenEmpty()
	{
		// Inform current wave that gameScreen is empty of "main" units (e.g. useful for ContinuousWave)
		currentWave.gameScreenEmpty();
		
		if (allSpawned())
			portal.setVisible(true);
		else if (currentWave.isFinished() && !upcomingWaves.isEmpty())
		{
			final Wave nextWave = upcomingWaves.get(0);
			nextWave.setWaveCountdown(Math.min(nextWave.getWaveCountdown(), MAX_WAVE_GAP));
		}
	}
	
	public void addObstacle(Obstacle obs) { obstacles.add(obs.copy()); }
	public void addEnemySpawnPoint(int x, int y)
	{
		enemySpawnPoints.add(new Point(x, y));
		availableSpawnPoints.add(enemySpawnPoints.size() - 1);
		spawnPointCDs.add(0);
	}
	public void setPlayerSpawnPoint(int x, int y) { playerSpawnPoint.set(x, y); }
	public void setPortalPoint(float x, float y) { portal.offsetTo(x, y); }
	public void setBossLevel(boolean bossLevel) { this.bossLevel = bossLevel; }
	public void clearPortal() { portal.setVisible(false); }
	
	public List<Obstacle> getObstacles() { return obstacles; }
	public boolean overlapsObstacle(Point p)
	{
		for (final Obstacle o : obstacles)
			if (o.contains(p))
				return true;
		
		return false;
	}
	public boolean isBossLevel() { return bossLevel; }
	public boolean isLevelComplete(Player player) { return (portal.isReady() && player.overlaps(portal)); }
	public Point getPlayerSpawnPoint() { return playerSpawnPoint; }
	public int getStageNum() { return stageNum; }
	public int getLevelNum() { return levelNum; }
	public int getWaveNum() { return waveCount - upcomingWaves.size(); }
	public int getWaveCount() { return waveCount; }
	public boolean allSpawned() { return (currentWave.isFinished() && upcomingWaves.isEmpty()) && enemiesBuffer.isEmpty(); }
	
	public void queueStevesUnit(AIUnit unit)
	{
		enemiesBuffer.add(unit);
	}
	
	public List<AIUnit> getEnemiesQueue()
	{
		enemiesReady.clear();
		enemiesReady.addAll(enemiesBuffer);
		enemiesBuffer.clear();
		return enemiesReady;
	}
	
	public void setBackground()
	{
		final SpriteManager manager = SpriteManager.getInstance();
		manager.clearBackgroundSprites();
		
		for (final Sprite s[] : backgroundTiles)
		{
			for (final Sprite ss : s)
				manager.addBackgroundSprite(ss.getTemplate(), ss.getX(), ss.getY());
		}
		
		for (final Obstacle o : obstacles)
			o.becomeVisible();
	}
	
	public void close()
	{
		SpriteManager.getInstance().clearBackgroundSprites();
	}
	
	private class MapNode
	{
		private final Point location;
		private final ArrayList<Integer> neighbors;
		private final int[] distances;
		
		public MapNode(Point point)
		{
			location = new Point(point);
			neighbors = new ArrayList<Integer>();
			distances = new int[nodesArraySize];
			for (int i=0; i<nodesArraySize; i++)
				distances[i] = Integer.MAX_VALUE;
		}
	}
}